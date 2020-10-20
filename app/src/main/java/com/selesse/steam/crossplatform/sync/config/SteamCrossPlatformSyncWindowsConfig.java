package com.selesse.steam.crossplatform.sync.config;

import java.nio.file.Path;

public class SteamCrossPlatformSyncWindowsConfig implements SteamCrossplatformSyncConfig {
    @Override
    public Path getLocalSyncLocation() {
        return null;
    }
}
