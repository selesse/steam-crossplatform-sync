package com.selesse.steam.appcache;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class StringCacheReader {
    private final Path path;
    private final long offset;

    public StringCacheReader(Path path, long offset) {
        this.path = path;
        this.offset = offset;
    }

    public StringCache read() throws IOException {
        StringCache stringCache = new StringCache();

        // The string table is read in one go rather than byte-by-byte through RandomAccessFile:
        // it's scanned in full regardless (every string in it gets parsed), so buffering it
        // ourselves and scanning the array directly avoids one native read call per byte.
        byte[] data;
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(path.toFile(), "r")) {
            randomAccessFile.seek(offset);
            data = new byte[(int) (randomAccessFile.length() - offset)];
            randomAccessFile.readFully(data);
        }

        int numberOfStrings =
                (data[0] & 0xFF) | ((data[1] & 0xFF) << 8) | ((data[2] & 0xFF) << 16) | ((data[3] & 0xFF) << 24);

        int pos = 4;
        while (pos < data.length) {
            int start = pos;
            while (pos < data.length && data[pos] != 0) {
                pos++;
            }
            if (pos >= data.length) {
                // Trailing bytes with no terminator - an incomplete final entry, discard it.
                break;
            }
            stringCache.append(new String(data, start, pos - start, StandardCharsets.UTF_8));
            pos++;
        }

        if (stringCache.size() != numberOfStrings) {
            throw new RuntimeException("Got " + stringCache.size() + " strings, but expected " + numberOfStrings);
        }
        return stringCache;
    }
}
