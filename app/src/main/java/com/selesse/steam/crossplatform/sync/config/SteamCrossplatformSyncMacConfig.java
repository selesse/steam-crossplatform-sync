package com.selesse.steam.crossplatform.sync.config;

import java.nio.file.Path;

public class SteamCrossplatformSyncMacConfig implements SteamCrossplatformSyncConfig {
    @Override
    public Path getConfigDirectory() {
        return Path.of(System.getProperty("user.home"), ".config", "steam-crossplatform-sync");
    }
}
