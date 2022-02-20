package com.selesse.steam.appcache;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class AppCacheBufferedReader implements Callable<AppCache> {
    private static final Byte BEGIN_OBJECT = 0;
    private static final Byte STRING = 1;
    private static final Byte INT_32 = 2;
    private static final Byte FLOAT_32 = 3;
    private static final Byte POINTER = 4;
    private static final Byte WIDESTRING = 5;
    private static final Byte COLOR = 6;
    private static final Byte INT_64 = 7;
    private static final Byte END_OBJECT = 8;
    private static final List<Byte> SPECIAL_BYTES = List.of(
            BEGIN_OBJECT, STRING, INT_32, FLOAT_32, POINTER, WIDESTRING, COLOR, INT_64, END_OBJECT
    );
    private final Path path;

    public AppCacheBufferedReader(Path path) {
        this.path = path;
    }

    @Override
    public AppCache call() throws Exception {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(path.toFile()))) {
            String firstFourBytes = readFourBytes(bufferedInputStream);
            assert firstFourBytes.equals("27 44 56 7");
            String nextByte = readFourBytes(bufferedInputStream);
            assert nextByte.equals("1 0 0 0");
            AppCache appCache = new AppCache();
            while (true) {
                int appId = parse32Int(bufferedInputStream);
                if (appId == 0) {
                    break;
                }
                int size = parse32Int(bufferedInputStream);
                int infoState = parse32Int(bufferedInputStream);
                int lastUpdated = parse32Int(bufferedInputStream);
                int picsToken = parse64Int(bufferedInputStream);
                byte[] sha1 = getSha1(bufferedInputStream);
                int changeNumber = parse32Int(bufferedInputStream);

                byte b = parseOneByte(bufferedInputStream);
                assert b == BEGIN_OBJECT;
                VdfObject object = parseVdfObject(bufferedInputStream);
                b = parseOneByte(bufferedInputStream);
                assert b == END_OBJECT;

                appCache.add(new App(appId, size, infoState, lastUpdated, picsToken, sha1, changeNumber, object));
            }

            return appCache;
        }
    }

    private VdfObject parseVdfObject(BufferedInputStream bufferedInputStream) throws IOException {
        List<Byte> currentData = getBytes(bufferedInputStream);
        String keyName = new String(byteListToByteArray(currentData));
        VdfObject vdfObject = new VdfObject(keyName);

        byte nextByte;
        while ((nextByte = parseOneByte(bufferedInputStream)) != END_OBJECT) {
            if (nextByte == BEGIN_OBJECT) {
                VdfObject nestedObject = parseVdfObject(bufferedInputStream);
                vdfObject.add(nestedObject);
            } else if (nextByte == STRING) {
                VdfString vdfString = parseStringValue(bufferedInputStream);
                vdfObject.add(vdfString);
            } else if (nextByte == INT_32) {
                VdfInteger vdfIntValue = parseIntValue(bufferedInputStream);
                vdfObject.add(vdfIntValue);
            } else {
                throw new RuntimeException("Unhandled parsing for byte " + nextByte);
            }
        }

        return vdfObject;
    }

    private List<Byte> getBytes(BufferedInputStream bufferedInputStream) throws IOException {
        List<Byte> currentData = new ArrayList<>();
        byte nextByte = parseOneByte(bufferedInputStream);
        while (!SPECIAL_BYTES.contains(nextByte)) {
            currentData.add(nextByte);
            nextByte = parseOneByte(bufferedInputStream);
        }
        return currentData;
    }

    private VdfInteger parseIntValue(BufferedInputStream bufferedInputStream) throws IOException {
        List<Byte> bytes = getBytes(bufferedInputStream);
        String keyName = new String(byteListToByteArray(bytes));
        int value = parse32Int(bufferedInputStream);
        return new VdfInteger(keyName, value);
    }

    private VdfString parseStringValue(BufferedInputStream bufferedInputStream) throws IOException {
        List<Byte> bytes = getBytes(bufferedInputStream);
        String keyName = new String(byteListToByteArray(bytes));
        List<Byte> bytes1 = getBytes(bufferedInputStream);
        String value = new String(byteListToByteArray(bytes1));
        return new VdfString(keyName, value);
    }

    private String readFourBytes(BufferedInputStream bufferedInputStream) throws IOException {
        byte[] magicBytes = bufferedInputStream.readNBytes(4);
        List<String> magicByteValues = new ArrayList<>();
        for (byte magicByte : magicBytes) {
            magicByteValues.add(Integer.toHexString(magicByte));
        }
        return String.join(" ", magicByteValues);
    }

    private byte[] getSha1(BufferedInputStream bufferedInputStream) throws IOException {
        return bufferedInputStream.readNBytes(20);
    }

    private byte[] byteListToByteArray(List<Byte> byteList) {
        byte[] bytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            bytes[i] = byteList.get(i);
        }
        return bytes;
    }

    private int parse32Int(BufferedInputStream bufferedInputStream) throws IOException {
        byte[] bytes = bufferedInputStream.readNBytes(4);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private int parse64Int(BufferedInputStream bufferedInputStream) throws IOException {
        byte[] bytes = bufferedInputStream.readNBytes(8);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private byte parseOneByte(BufferedInputStream bufferedInputStream) throws IOException {
        byte[] oneByte = bufferedInputStream.readNBytes(1);
        return oneByte[0];
    }
}
