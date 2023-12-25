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
            case LINUX -> "~/.steam";
                // TODO: for Steam OS, generate path based on Windows like so:
                // If it's SteamOS, it's:
                // $HOME/.local/share/Steam/steamapps/compatdata/<steamlappid>/pfx/drive_c/users/steamuser/
                // If it doesn't have Steam Auto-Cloud configured, the cloud stuff is saved in:
                // $HOME/.steam/steam/userdata/<user id>/<steam app id>/remote
            case STEAM_OS -> "~/.steam/steam";
        };
    }
}
