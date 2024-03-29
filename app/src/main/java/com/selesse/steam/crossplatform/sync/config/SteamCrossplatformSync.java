package com.selesse.steam.crossplatform.sync.config;

import com.selesse.os.OperatingSystems;

public class SteamCrossplatformSync {
    public static SteamCrossplatformSyncConfig loadConfiguration() {
        return switch (OperatingSystems.get()) {
            case MAC, LINUX, STEAM_OS -> new SteamCrossplatformSyncUnixConfig();
            case WINDOWS -> new SteamCrossPlatformSyncWindowsConfig();
        };
    }
}
