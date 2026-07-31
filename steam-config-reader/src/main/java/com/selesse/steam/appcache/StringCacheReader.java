package com.selesse.steam.appcache;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class StringCacheReader {
    private final Path path;
    private final byte[] data;
    private final int offset;

    public StringCacheReader(Path path, long offset) {
        this.path = path;
        this.data = null;
        this.offset = (int) offset;
    }

    /**
     * Reads the string table out of bytes already loaded into memory (e.g. by a prior
     * Files.readAllBytes) rather than re-reading the file from disk. Avoids a second I/O pass and
     * a possible inconsistency if the file changed between the two reads.
     */
    public StringCacheReader(byte[] fileData, long offset) {
        this.path = null;
        this.data = fileData;
        this.offset = (int) offset;
    }

    public StringCache read() throws IOException {
        StringCache stringCache = new StringCache();

        byte[] data = this.data;
        int start = offset;
        if (data == null) {
            // The string table is read in one go rather than byte-by-byte through
            // RandomAccessFile: it's scanned in full regardless (every string in it gets parsed),
            // so buffering it ourselves and scanning the array directly avoids one native read
            // call per byte.
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(path.toFile(), "r")) {
                randomAccessFile.seek(offset);
                data = new byte[(int) (randomAccessFile.length() - offset)];
                randomAccessFile.readFully(data);
            }
            start = 0;
        }

        int numberOfStrings = (data[start] & 0xFF)
                | ((data[start + 1] & 0xFF) << 8)
                | ((data[start + 2] & 0xFF) << 16)
                | ((data[start + 3] & 0xFF) << 24);

        int pos = start + 4;
        while (pos < data.length) {
            int stringStart = pos;
            while (pos < data.length && data[pos] != 0) {
                pos++;
            }
            if (pos >= data.length) {
                // Trailing bytes with no terminator - an incomplete final entry, discard it.
                break;
            }
            stringCache.append(new String(data, stringStart, pos - stringStart, StandardCharsets.UTF_8));
            pos++;
        }

        if (stringCache.size() != numberOfStrings) {
            throw new RuntimeException("Got " + stringCache.size() + " strings, but expected " + numberOfStrings);
        }
        return stringCache;
    }
}
