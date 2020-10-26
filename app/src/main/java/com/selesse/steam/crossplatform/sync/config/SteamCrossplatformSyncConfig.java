package com.selesse.steam.crossplatform.sync.config;

import com.selesse.steam.crossplatform.sync.cloud.CloudSyncLocationSupplier;
import com.selesse.steam.crossplatform.sync.serialize.ConfigRaw;

import java.nio.file.Path;
import java.util.Objects;

public interface SteamCrossplatformSyncConfig {
    Path getConfigDirectory();
    default Path getConfigFileLocation() {
        return Path.of(getConfigDirectory().toAbsolutePath().toString(), "config.yml");
    }

    /**
     * @return The absolute path to where we should be syncing games config files to, which
     * incorporates {@link #getCloudStorageRelativeWritePath()}}
     */
    default Path getLocalCloudSyncBaseDirectory() {
        return ConfigLoader.loadIfExists(getConfigFileLocation())
                .map(ConfigRaw::getPathToCloudStorage)
                .filter(Objects::nonNull)
                .map(Path::of)
                .orElse(CloudSyncLocationSupplier.get(this::getCloudStorageRelativeWritePath).orElseThrow());
    }

    // Which folder to write into the cloud storage, relative to the root
    default Path getCloudStorageRelativeWritePath() {
        return ConfigLoader.loadIfExists(getConfigFileLocation())
                .map(ConfigRaw::getCloudStorageRelativeWritePath)
                .filter(Objects::nonNull)
                .map(Path::of)
                .orElse(Path.of("steam-crossplatform-sync"));
    }

    default Path getGamesFile() {
        return ConfigLoader.loadIfExists(getConfigFileLocation())
                .map(ConfigRaw::getGamesFileLocation)
                .filter(Objects::nonNull)
                .map(Path::of)
                .orElse(Path.of(getLocalCloudSyncBaseDirectory().toAbsolutePath().toString(), "/games.yml"));
    }
}
