package com.selesse.steam.appcache;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        InputStream entryStream = new ByteArrayInputStream(entryBytes);
        int infoState = parse32Int(entryStream);
        int lastUpdated = parse32Int(entryStream);
        long picsToken = parse64Long(entryStream);
        byte[] sha1 = getSha1(entryStream);
        int changeNumber = parse32Int(entryStream);
        byte[] sha1Binary = null;
        if (parseSha1Binary) {
            sha1Binary = getSha1(entryStream);
        }

        byte b = parseOneByte(entryStream);
        if (b != BEGIN_OBJECT) {
            throw new IllegalStateException("Expected BEGIN_OBJECT for appId=" + appId + " but got " + b);
        }
        VdfObject object = parseVdfObject(entryStream, stringCache);
        b = parseOneByte(entryStream);
        if (b != END_OBJECT) {
            throw new IllegalStateException("Expected END_OBJECT for appId=" + appId + " but got " + b);
        }

        return new App(appId, size, infoState, lastUpdated, picsToken, sha1, changeNumber, sha1Binary, object);
    }

    private VdfObject parseVdfObject(InputStream inputStream, StringCache stringCache) throws IOException {
        String keyName = readKeyName(inputStream, stringCache);
        VdfObject vdfObject = new VdfObject(keyName);

        byte nextByte;
        while ((nextByte = parseOneByte(inputStream)) != END_OBJECT) {
            if (nextByte == BEGIN_OBJECT) {
                VdfObject nestedObject = parseVdfObject(inputStream, stringCache);
                vdfObject.add(nestedObject);
            } else if (nextByte == STRING) {
                vdfObject.add(parseStringValue(inputStream, stringCache));
            } else if (nextByte == INT_32) {
                vdfObject.add(parseIntValue(inputStream, stringCache));
            } else if (nextByte == FLOAT_32) {
                vdfObject.add(parseFloatValue(inputStream, stringCache));
            } else if (nextByte == INT_64) {
                vdfObject.add(parseLongValue(inputStream, stringCache));
            } else if (nextByte == POINTER || nextByte == COLOR) {
                vdfObject.add(parseIntValue(inputStream, stringCache));
            } else if (nextByte == WIDESTRING) {
                vdfObject.add(parseWideStringValue(inputStream, stringCache));
            } else {
                throw new IllegalStateException(
                        "Unhandled parsing for byte while parsing key=" + keyName + " => " + nextByte);
            }
        }

        return vdfObject;
    }

    private String readKeyName(InputStream inputStream, StringCache stringCache) throws IOException {
        if (stringCache == null) {
            return new String(getBytes(inputStream), StandardCharsets.UTF_8);
        } else {
            int index = parse32Int(inputStream);
            return stringCache.get(index);
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream currentData = new ByteArrayOutputStream();
        byte nextByte = parseOneByte(inputStream);
        while (nextByte < BEGIN_OBJECT || nextByte > END_OBJECT) {
            currentData.write(nextByte);
            nextByte = parseOneByte(inputStream);
        }
        return currentData.toByteArray();
    }

    private VdfInteger parseIntValue(InputStream inputStream, StringCache stringCache) throws IOException {
        String keyName = readKeyName(inputStream, stringCache);
        int value = parse32Int(inputStream);
        return new VdfInteger(keyName, value);
    }

    private VdfFloat parseFloatValue(InputStream inputStream, StringCache stringCache) throws IOException {
        String keyName = readKeyName(inputStream, stringCache);
        byte[] bytes = inputStream.readNBytes(4);
        float value = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        return new VdfFloat(keyName, value);
    }

    private VdfLong parseLongValue(InputStream inputStream, StringCache stringCache) throws IOException {
        String keyName = readKeyName(inputStream, stringCache);
        long value = parse64Long(inputStream);
        return new VdfLong(keyName, value);
    }

    private VdfString parseStringValue(InputStream inputStream, StringCache stringCache) throws IOException {
        String keyName = readKeyName(inputStream, stringCache);
        String value = new String(getBytes(inputStream), StandardCharsets.UTF_8);
        return new VdfString(keyName, value);
    }

    private VdfString parseWideStringValue(InputStream inputStream, StringCache stringCache) throws IOException {
        String keyName = readKeyName(inputStream, stringCache);
        byte[] bytes = getWideBytes(inputStream);
        String value = new String(bytes, StandardCharsets.UTF_16LE);
        return new VdfString(keyName, value);
    }

    private byte[] getWideBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        while (true) {
            byte[] codeUnit = inputStream.readNBytes(2);
            if (codeUnit.length < 2) {
                throw new IOException("Unexpected end of stream while reading a wide string");
            }
            if (codeUnit[0] == 0 && codeUnit[1] == 0) {
                return buffer.toByteArray();
            }
            buffer.write(codeUnit);
        }
    }

    private String readFourBytes(InputStream inputStream) throws IOException {
        byte[] magicBytes = inputStream.readNBytes(4);
        List<String> magicByteValues = new ArrayList<>();
        for (byte magicByte : magicBytes) {
            magicByteValues.add(Integer.toHexString(magicByte));
        }
        return String.join(" ", magicByteValues);
    }

    private byte[] getSha1(InputStream inputStream) throws IOException {
        return inputStream.readNBytes(20);
    }

    private int parse32Int(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readNBytes(4);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private long parse64Long(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readNBytes(8);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    private byte parseOneByte(InputStream inputStream) throws IOException {
        int value = inputStream.read();
        if (value == -1) {
            throw new EOFException("Unexpected end of stream");
        }
        return (byte) value;
    }
}
