package com.selesse.steam.appcache;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;
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
 *
 * read() parses the whole cache concurrently: entries aren't independently seekable (each one's
 * size is only known after reading its own header), so a single thread first indexes every
 * entry's offset, then hands entries out in byte-size-balanced chunks (not equal-count chunks -
 * entry size varies enough that a naive split leaves one thread doing most of the work) to worker
 * threads parsing concurrently against a shared, read-only copy of the file. readOne()/readSome()
 * stay single-threaded and skip-based - skipping bytes you don't need is cheaper than parsing them
 * on any number of cores, so a targeted lookup is faster than even a fully parallel full read.
 */
public class AppCacheBufferedReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppCacheBufferedReader.class);

    private static final byte BEGIN_OBJECT = 0;
    private static final byte STRING = 1;
    private static final byte INT_32 = 2;
    private static final byte FLOAT_32 = 3;
    private static final byte POINTER = 4;
    private static final byte WIDESTRING = 5;
    private static final byte COLOR = 6;
    private static final byte INT_64 = 7;
    private static final byte END_OBJECT = 8;
    private static final String VERSION_MARKER = "1 0 0 0";
    private final Path path;

    public AppCacheBufferedReader(Path path) {
        this.path = path;
    }

    public AppCache read() throws IOException {
        return read(Runtime.getRuntime().availableProcessors());
    }

    /** Reads a single app by ID, skipping every other entry's bytes without parsing them. */
    public Optional<App> readOne(long targetAppId) throws IOException {
        return Optional.ofNullable(readSome(Set.of(targetAppId)).get(targetAppId));
    }

    /**
     * Reads a specific set of apps by ID in a single pass, skipping every non-matching entry's
     * bytes without parsing them, and stopping as soon as every requested ID has been found.
     */
    public Map<Long, App> readSome(Set<Long> targetAppIds) throws IOException {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(path.toFile()))) {
            Header header = readHeader(bufferedInputStream, null);
            SomeApps selector = new SomeApps(targetAppIds);
            scanAppEntries(bufferedInputStream, header, selector);
            return selector.results();
        }
    }

    /**
     * Reads the first app satisfying the given predicate, parsing entries in file order until a
     * match is found. Unlike readOne()/readSome(), a match here isn't decidable from the entry
     * header alone, so every entry is parsed until one is found (or the file is exhausted).
     */
    public Optional<App> readFirst(Predicate<App> predicate) throws IOException {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(path.toFile()))) {
            Header header = readHeader(bufferedInputStream, null);
            FirstMatch selector = new FirstMatch(predicate);
            scanAppEntries(bufferedInputStream, header, selector);
            return Optional.ofNullable(selector.result());
        }
    }

    private record Header(boolean parseSha1Binary, StringCache stringCache) {}

    /**
     * Reads the header from a stream positioned at its start. If {@code fullFileData} is non-null
     * (the caller already has the whole file in memory, e.g. via {@link Files#readAllBytes}), the
     * string table is read out of it directly instead of re-reading the file from disk - avoiding
     * a second I/O pass and a possible inconsistency if the file changed between the two reads.
     */
    private Header readHeader(InputStream inputStream, byte[] fullFileData) throws IOException {
        String firstFourBytes = readFourBytes(inputStream);
        AppCacheFormat appCacheFormat = AppCacheFormat.fromFirstFourBytes(firstFourBytes);
        boolean parseSha1Binary = appCacheFormat.isAtLeast(AppCacheFormat.TWENTY_EIGHT);
        String versionMarker = readFourBytes(inputStream);
        if (!versionMarker.equals(VERSION_MARKER)) {
            throw new IllegalStateException(
                    "Expected version marker '" + VERSION_MARKER + "' but got '" + versionMarker + "'");
        }
        StringCache stringCache = null;
        if (appCacheFormat.isAtLeast(AppCacheFormat.TWENTY_NINE)) {
            long offsetToStringTable = parse64Long(inputStream);
            StringCacheReader stringCacheReader = fullFileData != null
                    ? new StringCacheReader(fullFileData, offsetToStringTable)
                    : new StringCacheReader(path, offsetToStringTable);
            stringCache = stringCacheReader.read();
        }
        return new Header(parseSha1Binary, stringCache);
    }

    /**
     * Parses the whole cache using an explicit worker thread count instead of the default of
     * {@link Runtime#availableProcessors()}. See {@link #read()}.
     */
    public AppCache read(int parallelism) throws IOException {
        byte[] data = Files.readAllBytes(path);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        Header header = readHeader(byteArrayInputStream, data);
        int entriesStart = data.length - byteArrayInputStream.available();

        List<EntryLocation> entries = indexEntries(data, entriesStart);
        AppCache appCache = new AppCache();
        if (entries.isEmpty()) {
            return appCache;
        }

        int workerCount = Math.max(1, Math.min(parallelism, entries.size()));
        List<List<EntryLocation>> chunks = partitionByBytes(entries, workerCount);

        ExecutorService executor = Executors.newFixedThreadPool(workerCount);
        try {
            List<Future<Map<Long, App>>> futures = new ArrayList<>();
            for (List<EntryLocation> chunk : chunks) {
                futures.add(executor.submit(() -> parseChunk(data, chunk, header)));
            }
            for (Future<Map<Long, App>> future : futures) {
                for (App app : awaitResult(future).values()) {
                    appCache.add(app);
                }
            }
        } finally {
            executor.shutdown();
        }
        return appCache;
    }

    private record EntryLocation(int appId, int entryStart, int size) {}

    /** Walks entry headers only (no VDF parsing) to record where each entry's bytes live. */
    private List<EntryLocation> indexEntries(byte[] data, int start) {
        List<EntryLocation> entries = new ArrayList<>();
        int pos = start;
        while (pos + 8 <= data.length) {
            int appId = readInt32LE(data, pos);
            if (appId == 0) {
                break;
            }
            int size = readInt32LE(data, pos + 4);
            int entryStart = pos + 8;
            entries.add(new EntryLocation(appId, entryStart, size));
            pos = entryStart + size;
        }
        return entries;
    }

    private static int readInt32LE(byte[] data, int offset) {
        return (data[offset] & 0xFF)
                | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 2] & 0xFF) << 16)
                | ((data[offset + 3] & 0xFF) << 24);
    }

    // Tracks one chunk's entries alongside its running byte total, so partitionByBytes can find
    // the currently-smallest chunk without maintaining a second, index-parallel array.
    private static final class Bucket {
        private final List<EntryLocation> entries = new ArrayList<>();
        private long totalBytes;

        void add(EntryLocation entry) {
            entries.add(entry);
            totalBytes += entry.size();
        }
    }

    // Balances total entry bytes per chunk rather than entry count: entry size is a decent proxy
    // for parse cost (more bytes roughly means more VDF fields to walk), so an even split by count
    // alone can leave one thread with a disproportionate share of the file's largest entries.
    // Greedy LPT (largest-first, always into the currently-smallest chunk) keeps chunks close to
    // balanced without the cost of computing an optimal partition.
    private static List<List<EntryLocation>> partitionByBytes(List<EntryLocation> entries, int chunkCount) {
        List<EntryLocation> bySizeDescending = new ArrayList<>(entries);
        bySizeDescending.sort(Comparator.comparingInt(EntryLocation::size).reversed());

        List<Bucket> buckets = new ArrayList<>(chunkCount);
        for (int i = 0; i < chunkCount; i++) {
            buckets.add(new Bucket());
        }

        for (EntryLocation entry : bySizeDescending) {
            Bucket smallest = buckets.get(0);
            for (Bucket bucket : buckets) {
                if (bucket.totalBytes < smallest.totalBytes) {
                    smallest = bucket;
                }
            }
            smallest.add(entry);
        }
        return buckets.stream().map(bucket -> bucket.entries).toList();
    }

    private Map<Long, App> parseChunk(byte[] data, List<EntryLocation> chunk, Header header) {
        Map<Long, App> results = new HashMap<>();
        for (EntryLocation location : chunk) {
            byte[] entryBytes =
                    Arrays.copyOfRange(data, location.entryStart(), location.entryStart() + location.size());
            try {
                App app = parseAppEntry(location.appId(), location.size(), entryBytes, header);
                results.put((long) location.appId(), app);
            } catch (Exception e) {
                LOGGER.warn("Skipping app cache entry for appId={} because it failed to parse", location.appId(), e);
            }
        }
        return results;
    }

    private static Map<Long, App> awaitResult(Future<Map<Long, App>> future) throws IOException {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for parallel app cache parsing", e);
        } catch (ExecutionException e) {
            throw new IOException("Parallel app cache parsing failed", e.getCause());
        }
    }

    private void scanAppEntries(BufferedInputStream bufferedInputStream, Header header, AppQuery selector)
            throws IOException {
        while (!selector.isDone()) {
            int appId;
            int size;
            try {
                appId = parse32Int(bufferedInputStream);
                if (appId == 0) {
                    return;
                }
                size = parse32Int(bufferedInputStream);
            } catch (Exception e) {
                LOGGER.warn("Stopping app cache scan after failing to locate the next entry boundary", e);
                return;
            }

            if (selector.shouldParse(appId)) {
                byte[] entryBytes = bufferedInputStream.readNBytes(size);
                try {
                    selector.onParsed(appId, parseAppEntry(appId, size, entryBytes, header));
                } catch (Exception e) {
                    LOGGER.warn("Skipping app cache entry for appId={} because it failed to parse", appId, e);
                }
            } else {
                bufferedInputStream.skipNBytes(size);
            }
        }
    }

    private App parseAppEntry(int appId, int size, byte[] entryBytes, Header header) throws IOException {
        EntryCursor cursor = new EntryCursor(entryBytes);
        int infoState = cursor.readInt32();
        int lastUpdated = cursor.readInt32();
        long picsToken = cursor.readInt64();
        byte[] sha1 = cursor.readSha1();
        int changeNumber = cursor.readInt32();
        byte[] sha1Binary = null;
        if (header.parseSha1Binary()) {
            sha1Binary = cursor.readSha1();
        }

        byte b = cursor.readByte();
        if (b != BEGIN_OBJECT) {
            throw new IllegalStateException("Expected BEGIN_OBJECT for appId=" + appId + " but got " + b);
        }
        VdfObject object = parseVdfObject(cursor, header.stringCache());
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
            switch (nextByte) {
                case BEGIN_OBJECT -> vdfObject.add(parseVdfObject(cursor, stringCache));
                case STRING -> vdfObject.add(parseStringValue(cursor, stringCache));
                case INT_32, POINTER, COLOR -> vdfObject.add(parseIntValue(cursor, stringCache));
                case FLOAT_32 -> vdfObject.add(parseFloatValue(cursor, stringCache));
                case INT_64 -> vdfObject.add(parseLongValue(cursor, stringCache));
                case WIDESTRING -> vdfObject.add(parseWideStringValue(cursor, stringCache));
                default ->
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

        // Scans the local array/index directly instead of going through readByte(), since this is
        // called once per byte of every string value across every app in the cache.
        void skipCString() throws EOFException {
            int p = pos;
            int length = data.length;
            while (p < length) {
                byte b = data[p++];
                if (b >= BEGIN_OBJECT && b <= END_OBJECT) {
                    pos = p;
                    return;
                }
            }
            throw new EOFException("Unexpected end of stream");
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
