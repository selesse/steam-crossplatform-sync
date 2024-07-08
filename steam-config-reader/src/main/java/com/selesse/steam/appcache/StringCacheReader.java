package com.selesse.steam.appcache;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StringCacheReader {
    private final Path path;
    private final long offset;

    public StringCacheReader(Path path, long offset) {
        this.path = path;
        this.offset = offset;
    }

    public StringCache read() throws IOException {
        StringCache stringCache = new StringCache();

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(path.toFile(), "r")) {
            randomAccessFile.seek(offset);
            int numberOfStrings = parse32Int(randomAccessFile);

            List<byte[]> byteArrays = new ArrayList<>();
            boolean eofReached = false;

            while (!eofReached) {
                try {
                    byte[] bytes = readBytesUntilZero(randomAccessFile);
                    byteArrays.add(bytes);
                } catch (EOFException e) {
                    eofReached = true;
                }
            }

            byteArrays.stream().map(x -> new String(x, StandardCharsets.UTF_8)).forEach(stringCache::append);
            if (stringCache.size() != numberOfStrings) {
                throw new RuntimeException("Got " + stringCache.size() + " strings, but expected " + numberOfStrings);
            }
            return stringCache;
        }
    }

    private int parse32Int(RandomAccessFile randomAccessFile) throws IOException {
        byte[] bytes = new byte[4];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = randomAccessFile.readByte();
        }
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private static byte[] readBytesUntilZero(RandomAccessFile randomAccessFile) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int currentByte;

        while ((currentByte = randomAccessFile.read()) != -1) {
            if (currentByte == (byte) 0) {
                return buffer.toByteArray();
            }
            buffer.write(currentByte);
        }

        throw new EOFException();
    }
}
