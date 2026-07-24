package com.selesse.steam.appcache;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses Steam's binary {@code appinfo.vdf} app cache.
 *
 * <pre>
 * +------------------------------------------------------------------------+
 * | HEADER                                                                 |
 * |                                                                        |
 * |  magic(4B: 27/28/29) + "1 0 0 0"(4B) + [stringTableOffset(8B) fmt>=29] |
 * |                                                                        |
 * | APP ENTRIES (repeated until appId == 0)                                |
 * |  appId(4B) + size(4B) + entryBytes[size] ---> see ENTRY BODY below     |
 * |                                                                        |
 * | STRING TABLE (fmt >= 29 only, near EOF, at stringTableOffset)          |
 * |  count(4B) + count x null-terminated UTF-8 strings                     |
 * |  (referenced by index instead of inlining key names in each entry)     |
 * +------------------------------------------------------------------------+
 *
 * ENTRY BODY (entryBytes[size], read with EntryCursor)
 * +--------------+-------------+--------------+----------+--------------+----------------+
 * | infoState 4B | lastUpdated | picsToken 8B | sha1 20B | changeNumber | sha1Binary 20B |
 * |              | 4B          |              |          | 4B           | (fmt>=28 only) |
 * +--------------+-------------+--------------+----------+--------------+----------------+
 * followed by: BEGIN_OBJECT, a recursive VDF tree, END_OBJECT
 *
 * VDF TREE NODE - keyName then typed fields until END_OBJECT
 *   keyName = stringCache[readInt32()]   (fmt >= 29)
 *           | readCString()              (fmt below 29)
 *
 *   0x00 BEGIN_OBJECT  nested VdfObject (recurse)
 *   0x01 STRING        key + C-string (UTF-8)
 *   0x02 INT_32        key + int32 LE
 *   0x03 FLOAT_32      key + int32 bits reinterpreted as float
 *   0x04 POINTER       key + int32
 *   0x05 WIDESTRING    key + UTF-16LE, terminated by 0x0000
 *   0x06 COLOR         key + int32
 *   0x07 INT_64        key + int64 LE
 *   0x08 END_OBJECT    stop, return to parent
 * </pre>
 */
public class AppCacheBufferedReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppCacheBufferedReader.class);

    private static final Byte BEGIN_OBJECT = 0;
    private static final Byte STRING = 1;
    private static final Byte INT_32 = 2;
    private static final Byte FLOAT_32 = 3;
    private static final Byte POINTER = 4;
    private static final Byte WIDESTRING = 5;
    private static final Byte COLOR = 6;
    private static final Byte INT_64 = 7;
    private static final Byte END_OBJECT = 8;
    private static final String VERSION_MARKER = "1 0 0 0";
    private final Path path;

    public AppCacheBufferedReader(Path path) {
        this.path = path;
    }

    public AppCache read() throws IOException {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(path.toFile()))) {
            String firstFourBytes = readFourBytes(bufferedInputStream);
            AppCacheFormat appCacheFormat = AppCacheFormat.fromFirstFourBytes(firstFourBytes);
            boolean parseSha1Binary = appCacheFormat.isAtLeast(AppCacheFormat.TWENTY_EIGHT);
            String versionMarker = readFourBytes(bufferedInputStream);
            if (!versionMarker.equals(VERSION_MARKER)) {
                throw new IllegalStateException(
                        "Expected version marker '" + VERSION_MARKER + "' but got '" + versionMarker + "'");
            }
            AppCache appCache = new AppCache();
            StringCache stringCache = null;
            if (appCacheFormat.isAtLeast(AppCacheFormat.TWENTY_NINE)) {
                long offsetToStringTable = parse64Long(bufferedInputStream);
                stringCache = new StringCacheReader(path, offsetToStringTable).read();
            }

            readAppEntries(bufferedInputStream, parseSha1Binary, stringCache, appCache);

            return appCache;
        }
    }

    private void readAppEntries(
            BufferedInputStream bufferedInputStream,
            boolean parseSha1Binary,
            StringCache stringCache,
            AppCache appCache)
            throws IOException {
        while (true) {
            int appId;
            int size;
            byte[] entryBytes;
            try {
                appId = parse32Int(bufferedInputStream);
                if (appId == 0) {
                    return;
                }
                size = parse32Int(bufferedInputStream);
                entryBytes = bufferedInputStream.readNBytes(size);
            } catch (Exception e) {
                LOGGER.warn(
                        "Stopping app cache read after failing to locate the next entry boundary,"
                                + " keeping the {} app(s) parsed so far",
                        appCache.size(),
                        e);
                return;
            }

            try {
                appCache.add(parseAppEntry(appId, size, entryBytes, parseSha1Binary, stringCache));
            } catch (Exception e) {
                LOGGER.warn("Skipping app cache entry for appId={} because it failed to parse", appId, e);
            }
        }
    }

    private App parseAppEntry(int appId, int size, byte[] entryBytes, boolean parseSha1Binary, StringCache stringCache)
            throws IOException {
        EntryCursor cursor = new EntryCursor(entryBytes);
        int infoState = cursor.readInt32();
        int lastUpdated = cursor.readInt32();
        long picsToken = cursor.readInt64();
        byte[] sha1 = cursor.readSha1();
        int changeNumber = cursor.readInt32();
        byte[] sha1Binary = null;
        if (parseSha1Binary) {
            sha1Binary = cursor.readSha1();
        }

        byte b = cursor.readByte();
        if (b != BEGIN_OBJECT) {
            throw new IllegalStateException("Expected BEGIN_OBJECT for appId=" + appId + " but got " + b);
        }
        VdfObject object = parseVdfObject(cursor, stringCache);
        b = cursor.readByte();
        if (b != END_OBJECT) {
            throw new IllegalStateException("Expected END_OBJECT for appId=" + appId + " but got " + b);
        }

        return new App(appId, size, infoState, lastUpdated, picsToken, sha1, changeNumber, sha1Binary, object);
    }

    private VdfObject parseVdfObject(EntryCursor cursor, StringCache stringCache) throws IOException {
        String keyName = readKeyName(cursor, stringCache);
        VdfObject vdfObject = new VdfObject(keyName);

        byte nextByte;
        while ((nextByte = cursor.readByte()) != END_OBJECT) {
            if (nextByte == BEGIN_OBJECT) {
                VdfObject nestedObject = parseVdfObject(cursor, stringCache);
                vdfObject.add(nestedObject);
            } else if (nextByte == STRING) {
                vdfObject.add(parseStringValue(cursor, stringCache));
            } else if (nextByte == INT_32) {
                vdfObject.add(parseIntValue(cursor, stringCache));
            } else if (nextByte == FLOAT_32) {
                vdfObject.add(parseFloatValue(cursor, stringCache));
            } else if (nextByte == INT_64) {
                vdfObject.add(parseLongValue(cursor, stringCache));
            } else if (nextByte == POINTER || nextByte == COLOR) {
                vdfObject.add(parseIntValue(cursor, stringCache));
            } else if (nextByte == WIDESTRING) {
                vdfObject.add(parseWideStringValue(cursor, stringCache));
            } else {
                throw new IllegalStateException(
                        "Unhandled parsing for byte while parsing key=" + keyName + " => " + nextByte);
            }
        }

        return vdfObject;
    }

    private String readKeyName(EntryCursor cursor, StringCache stringCache) throws IOException {
        if (stringCache == null) {
            return cursor.readCString();
        } else {
            int index = cursor.readInt32();
            return stringCache.get(index);
        }
    }

    private VdfInteger parseIntValue(EntryCursor cursor, StringCache stringCache) throws IOException {
        String keyName = readKeyName(cursor, stringCache);
        int value = cursor.readInt32();
        return new VdfInteger(keyName, value);
    }

    private VdfFloat parseFloatValue(EntryCursor cursor, StringCache stringCache) throws IOException {
        String keyName = readKeyName(cursor, stringCache);
        float value = cursor.readFloat32();
        return new VdfFloat(keyName, value);
    }

    private VdfLong parseLongValue(EntryCursor cursor, StringCache stringCache) throws IOException {
        String keyName = readKeyName(cursor, stringCache);
        long value = cursor.readInt64();
        return new VdfLong(keyName, value);
    }

    private VdfString parseStringValue(EntryCursor cursor, StringCache stringCache) throws IOException {
        String keyName = readKeyName(cursor, stringCache);
        int start = cursor.position();
        cursor.skipCString();
        return new VdfString(keyName, cursor.backingArray(), start, cursor.position() - start - 1);
    }

    private VdfString parseWideStringValue(EntryCursor cursor, StringCache stringCache) throws IOException {
        String keyName = readKeyName(cursor, stringCache);
        String value = cursor.readWideString();
        return new VdfString(keyName, value);
    }

    private String readFourBytes(InputStream inputStream) throws IOException {
        byte[] magicBytes = inputStream.readNBytes(4);
        List<String> magicByteValues = new ArrayList<>();
        for (byte magicByte : magicBytes) {
            magicByteValues.add(Integer.toHexString(magicByte));
        }
        return String.join(" ", magicByteValues);
    }

    private int parse32Int(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readNBytes(4);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private long parse64Long(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readNBytes(8);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    // Indexes directly into the already-buffered entry bytes instead of wrapping them in an
    // InputStream: this is called once per field (and once per byte of every string) across every
    // app in the cache, so allocation-free access here is what keeps a full parse fast.
    private static final class EntryCursor {
        private final byte[] data;
        private int pos;

        EntryCursor(byte[] data) {
            this.data = data;
        }

        byte readByte() throws EOFException {
            if (pos >= data.length) {
                throw new EOFException("Unexpected end of stream");
            }
            return data[pos++];
        }

        int readInt32() throws EOFException {
            requireRemaining(4);
            int value = (data[pos] & 0xFF)
                    | ((data[pos + 1] & 0xFF) << 8)
                    | ((data[pos + 2] & 0xFF) << 16)
                    | ((data[pos + 3] & 0xFF) << 24);
            pos += 4;
            return value;
        }

        long readInt64() throws EOFException {
            requireRemaining(8);
            long value = 0;
            for (int i = 0; i < 8; i++) {
                value |= (data[pos + i] & 0xFFL) << (8 * i);
            }
            pos += 8;
            return value;
        }

        float readFloat32() throws EOFException {
            return Float.intBitsToFloat(readInt32());
        }

        byte[] readSha1() throws EOFException {
            requireRemaining(20);
            byte[] result = Arrays.copyOfRange(data, pos, pos + 20);
            pos += 20;
            return result;
        }

        String readCString() throws EOFException {
            int start = pos;
            skipCString();
            return new String(data, start, pos - start - 1, StandardCharsets.UTF_8);
        }

        void skipCString() throws EOFException {
            byte b = readByte();
            while (b < BEGIN_OBJECT || b > END_OBJECT) {
                b = readByte();
            }
        }

        int position() {
            return pos;
        }

        byte[] backingArray() {
            return data;
        }

        String readWideString() throws EOFException {
            int start = pos;
            while (true) {
                requireRemaining(2);
                byte lo = data[pos];
                byte hi = data[pos + 1];
                pos += 2;
                if (lo == 0 && hi == 0) {
                    break;
                }
            }
            return new String(data, start, pos - start - 2, StandardCharsets.UTF_16LE);
        }

        private void requireRemaining(int length) throws EOFException {
            if (pos + length > data.length) {
                throw new EOFException("Unexpected end of stream");
            }
        }
    }
}
