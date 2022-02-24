package com.selesse.steam.games;

class SteamPathConverter {
    public static String convert(String path) {
        // https://partner.steamgames.com/doc/features/cloud
        return path
                .replace("WinMyDocuments", "%USERPROFILE%/Documents")
                .replace("WinAppDataLocal", "%USERPROFILE%/AppData/Local")
                .replace("WinAppDataLocalLow", "%USERPROFILE%/AppData/LocalLow")
                .replace("WinAppDataRoaming", "%USERPROFILE%/AppData/Roaming")
                .replace("WinSavedGames", "%USERPROFILE%/Saved Games")
                .replace("MacHome", "~")
                .replace("MacAppSupport", "~/Library/Application Support")
                .replace("MacDocuments", "~/Documents")
                .replace("LinuxHome", "~")
                .replace("LinuxXdgDataHome", System.getenv().getOrDefault("XDG_DATA_HOME", "~/.config"));
    }
}
