package com.selesse.steam.appcache;

import java.util.Arrays;

public enum AppCacheFormat {
    TWENTY_SEVEN("27"),
    TWENTY_EIGHT("28"),
    TWENTY_NINE("29"),
    ;

    private final String number;

    AppCacheFormat(String number) {
        this.number = number;
    }

    public static AppCacheFormat fromFirstFourBytes(String bytes) {
        if (!bytes.endsWith("44 56 7")) {
            throw new IllegalStateException("First four bytes didn't end in '44 56 7'");
        }
        String magicNumber = bytes.split(" ")[0];
        return fromString(magicNumber);
    }

    private static AppCacheFormat fromString(String value) {
        return Arrays.stream(values())
                .filter(x -> x.number.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unsupported app cache format version " + value));
    }

    public boolean isAtLeast(AppCacheFormat appCacheFormat) {
        return this.ordinal() >= appCacheFormat.ordinal();
    }
}
