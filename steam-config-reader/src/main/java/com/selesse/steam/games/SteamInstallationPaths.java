package com.selesse.steam.games;

import com.selesse.os.OperatingSystems;

public class SteamInstallationPaths {
    public static String get(OperatingSystems.OperatingSystem operatingSystem) {
        return getRoot(operatingSystem) + "/steamapps/common";
    }

    public static String getRoot(OperatingSystems.OperatingSystem operatingSystem) {
        return switch (operatingSystem) {
            case MAC -> "~/Library/Application Support/Steam";
            case WINDOWS -> "%PROGRAMFILES(X86)%/Steam";
            // ~/.steam has no steamapps symlink of its own; only ~/.steam/steam does.
            case LINUX, STEAM_OS -> "~/.steam/steam";
        };
    }
}
