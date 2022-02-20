package com.selesse.steam.registry;

import com.selesse.os.OperatingSystems;

public enum SteamOperatingSystem {
    WINDOWS("windows"),
    MAC("macos"),
    LINUX("linux"),
    ;

    private final String steamValue;

    SteamOperatingSystem(String steamValue) {
        this.steamValue = steamValue;
    }

    public OperatingSystems.OperatingSystem toOperatingSystem() {
        return switch (this) {
            case MAC -> OperatingSystems.OperatingSystem.MAC;
            case WINDOWS -> OperatingSystems.OperatingSystem.WINDOWS;
            case LINUX -> OperatingSystems.OperatingSystem.LINUX;
        };
    }

    public static SteamOperatingSystem fromString(String string) {
        return switch (string.toLowerCase()) {
            case "linux" -> LINUX;
            case "macos" -> MAC;
            case "windows" -> WINDOWS;
            default -> throw new IllegalArgumentException("Unknown string " + string);
        };
    }

    public String steamValue() {
        return steamValue;
    }
}
