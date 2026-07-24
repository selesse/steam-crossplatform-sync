package com.selesse.steam.appcache;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

// Hand-built binary VDF fixtures: no real Steam dump we have exercises these field types or a corrupted entry.
public class AppCacheBufferedReaderSyntheticTest {
    private static final byte BEGIN_OBJECT = 0;
    private static final byte STRING = 1;
    private static final byte INT_32 = 2;
    private static final byte FLOAT_32 = 3;
    private static final byte POINTER = 4;
    private static final byte WIDESTRING = 5;
    private static final byte COLOR = 6;
    private static final byte INT_64 = 7;
    private static final byte END_OBJECT = 8;

    @Test
    public void parsesEveryFieldTypeAndWidePicsToken() throws Exception {
        long bigPicsToken = 5_000_000_000L; // exceeds 32 bits, exercises the INT_64 header field

        byte[] appinfoBody = concat(
                cString("appinfo"),
                intField("int_field", 42),
                floatField("float_field", 3.5f),
                longField("long_field", 9_000_000_000L),
                field(POINTER, "pointer_field", le4(7)),
                field(COLOR, "color_field", le4(255)),
                wideStringField("wide_field", "hi"),
                nestedObjectField("nested", stringField("inner", "value")),
                new byte[] {END_OBJECT});

        byte[] entry = appEntry(5, 1, 1_600_000_000, bigPicsToken, 42, appinfoBody);

        Path path = writeCacheFile(entry);

        AppCache appCache = new AppCacheBufferedReader(path).read();
        App app = appCache.getById(5);

        assertThat(app.picsToken()).isEqualTo(bigPicsToken);

        VdfObject object = app.vdfObject();
        assertThat(object.getValues())
                .contains(
                        new VdfInteger("int_field", 42),
                        new VdfFloat("float_field", 3.5f),
                        new VdfLong("long_field", 9_000_000_000L),
                        new VdfInteger("pointer_field", 7),
                        new VdfInteger("color_field", 255),
                        new VdfString("wide_field", "hi"));

        VdfObject nested = object.getValues().stream()
                .filter(VdfObject.class::isInstance)
                .map(VdfObject.class::cast)
                .findFirst()
                .orElseThrow();
        assertThat(nested.getName()).isEqualTo("nested");
        assertThat(nested.getValues()).containsExactly(new VdfString("inner", "value"));
    }

    @Test
    public void skipsUnparsableEntryButKeepsOthers() throws Exception {
        byte[] goodBodyA = concat(cString("appinfo"), intField("appid", 100), new byte[] {END_OBJECT});
        byte[] entryA = appEntry(100, 1, 1, 0, 1, goodBodyA);

        // Simulates a future Valve format adding a field type this reader doesn't understand yet.
        byte[] corruptBody = concat(cString("appinfo"), new byte[] {(byte) 99});
        byte[] entryB = appEntry(200, 1, 1, 0, 1, corruptBody);

        byte[] goodBodyC = concat(cString("appinfo"), intField("appid", 300), new byte[] {END_OBJECT});
        byte[] entryC = appEntry(300, 1, 1, 0, 1, goodBodyC);

        Path path = writeCacheFile(concat(entryA, entryB, entryC));

        AppCache appCache = new AppCacheBufferedReader(path).read();

        assertThat(appCache.size()).isEqualTo(2);
        assertThat(appCache.getById(100)).isNotNull();
        assertThat(appCache.getById(200)).isNull();
        assertThat(appCache.getById(300)).isNotNull();
    }

    private Path writeCacheFile(byte[]... entries) throws Exception {
        byte[] header = concat(new byte[] {0x27, 'D', 'V', 0x07}, le4(1));
        byte[] terminator = le4(0);
        byte[] contents = concat(header, concat(entries), terminator);

        Path path = Files.createTempFile("appcache-synthetic-test", ".vdf");
        path.toFile().deleteOnExit();
        Files.write(path, contents);
        return path;
    }

    private byte[] appEntry(
            int appId, int infoState, int lastUpdated, long picsToken, int changeNumber, byte[] appinfoBody) {
        byte[] sha1 = new byte[20];
        byte[] payload = concat(
                le4(infoState),
                le4(lastUpdated),
                le8(picsToken),
                sha1,
                le4(changeNumber),
                new byte[] {BEGIN_OBJECT},
                appinfoBody,
                new byte[] {END_OBJECT});

        return concat(le4(appId), le4(payload.length), payload);
    }

    private static byte[] field(byte tag, String key, byte[] valueBytes) {
        return concat(new byte[] {tag}, cString(key), valueBytes);
    }

    private static byte[] intField(String key, int value) {
        return field(INT_32, key, le4(value));
    }

    private static byte[] floatField(String key, float value) {
        return field(FLOAT_32, key, le4(Float.floatToIntBits(value)));
    }

    private static byte[] longField(String key, long value) {
        return field(INT_64, key, le8(value));
    }

    private static byte[] stringField(String key, String value) {
        return field(STRING, key, cString(value));
    }

    private static byte[] wideStringField(String key, String value) {
        byte[] utf16 = value.getBytes(StandardCharsets.UTF_16LE);
        return field(WIDESTRING, key, concat(utf16, new byte[] {0, 0}));
    }

    private static byte[] nestedObjectField(String key, byte[]... children) {
        return field(BEGIN_OBJECT, key, concat(concat(children), new byte[] {END_OBJECT}));
    }

    private static byte[] cString(String value) {
        return concat(value.getBytes(StandardCharsets.UTF_8), new byte[] {0});
    }

    private static byte[] le4(int value) {
        return ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(value)
                .array();
    }

    private static byte[] le8(long value) {
        return ByteBuffer.allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putLong(value)
                .array();
    }

    private static byte[] concat(byte[]... arrays) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] array : arrays) {
            out.writeBytes(array);
        }
        return out.toByteArray();
    }
}
