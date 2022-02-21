package com.selesse.steam.crossplatform.sync.config;

import java.nio.file.Path;
import java.util.Optional;

public class SteamCrossplatformSyncUnixConfig implements SteamCrossplatformSyncConfig {
    @Override
    public Path getConfigDirectory() {
        String configDirectory = Optional.ofNullable(System.getenv("XDG_CONFIG_HOME"))
                .orElse(System.getProperty("user.home") + "/.config");
        return Path.of(configDirectory, "steam-crossplatform-sync");
    }
}
