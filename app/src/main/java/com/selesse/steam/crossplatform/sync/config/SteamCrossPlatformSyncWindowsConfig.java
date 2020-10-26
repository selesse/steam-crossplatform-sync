package com.selesse.steam.crossplatform.sync.config;

import java.nio.file.Path;

public class SteamCrossPlatformSyncWindowsConfig implements SteamCrossplatformSyncConfig {
    @Override
    public Path getConfigDirectory() {
        return Path.of(System.getenv("LOCALAPPDATA"), "steam-crossplatform-sync");
    }
}
