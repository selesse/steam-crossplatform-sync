package com.selesse.steam.registry;

import com.selesse.os.OperatingSystems;
import java.util.Optional;

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
        return tryFromString(string).orElseThrow(() -> new IllegalArgumentException("Unknown string " + string));
    }

    // common/oslist in the app cache isn't limited to desktop platforms (e.g. some VR titles list
    // "android" for a companion app), so callers that scan it need a non-throwing lookup.
    public static Optional<SteamOperatingSystem> tryFromString(String string) {
        return switch (string.toLowerCase()) {
            case "linux" -> Optional.of(LINUX);
            case "macos" -> Optional.of(MAC);
            case "windows" -> Optional.of(WINDOWS);
            default -> Optional.empty();
        };
    }

    public String steamValue() {
        return steamValue;
    }
}
