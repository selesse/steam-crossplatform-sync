package com.selesse.steam.games;

import com.selesse.os.OperatingSystems;

public class SteamInstallationPaths {
    public static String get(OperatingSystems.OperatingSystem operatingSystem) {
        return switch (operatingSystem) {
            case MAC -> "~/Library/Application Support/Steam/steamapps/common";
            case WINDOWS -> "%PROGRAMFILES(X86)%/Steam/steamapps/common";
            case LINUX -> "~/.steam/steamapps/common";
        };
    }
}
