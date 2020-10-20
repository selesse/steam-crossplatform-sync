package com.selesse.steam.crossplatform.sync.config;

import com.selesse.steam.crossplatform.sync.cloud.CloudSyncLocationSupplier;
import com.selesse.steam.crossplatform.sync.serialize.ConfigRaw;

import java.nio.file.Path;
import java.util.Objects;

public interface SteamCrossplatformSyncConfig {
    Path getConfigLocation();

    /**
     * @return The absolute path to where we should be syncing games config files to, which
     * incorporates {@link #getCloudStorageRelativeWritePath()}}
     */
    default Path getLocalCloudSyncBaseDirectory() {
        return ConfigLoader.loadIfExists(getConfigLocation())
                .map(ConfigRaw::getPathToCloudStorage)
                .filter(Objects::nonNull)
                .map(Path::of)
                .orElse(CloudSyncLocationSupplier.get(this::getCloudStorageRelativeWritePath).orElseThrow());
    }

    // Which folder to write into the cloud storage, relative to the root
    default Path getCloudStorageRelativeWritePath() {
        return ConfigLoader.loadIfExists(getConfigLocation())
                .map(ConfigRaw::getCloudStorageRelativeWritePath)
                .filter(Objects::nonNull)
                .map(Path::of)
                .orElse(Path.of("steam-crossplatform-sync"));
    }

    default Path getGamesFile() {
        return ConfigLoader.loadIfExists(getConfigLocation())
                .map(ConfigRaw::getGamesFileLocation)
                .filter(Objects::nonNull)
                .map(Path::of)
                .orElse(Path.of(getLocalCloudSyncBaseDirectory().toAbsolutePath().toString(), "/games.yml"));
    }
}
