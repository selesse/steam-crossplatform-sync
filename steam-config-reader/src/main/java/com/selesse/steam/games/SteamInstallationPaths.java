package com.selesse.steam.games;

import com.selesse.os.OperatingSystems;

public class SteamInstallationPaths {
    public static String get(OperatingSystems.OperatingSystem operatingSystem) {
        if (operatingSystem == OperatingSystems.OperatingSystem.MAC) {
            return "~/Library/Application Support/Steam/steamapps/common";
        } else if (operatingSystem == OperatingSystems.OperatingSystem.WINDOWS) {
            // TODO: Probably should read this from the Steam registry...
            return "%PROGRAMFILES(X86)%/Steam/steamsapps/common";
        }
        throw new IllegalArgumentException("Unsupported Steam install path");
    }
}
