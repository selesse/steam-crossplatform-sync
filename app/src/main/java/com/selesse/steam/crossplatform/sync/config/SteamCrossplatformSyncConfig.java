package com.selesse.steam.crossplatform.sync.config;

import com.selesse.steam.crossplatform.sync.cloud.CloudSyncLocationSupplier;
import com.selesse.steam.crossplatform.sync.serialize.ConfigRaw;

import java.nio.file.Path;
import java.util.Objects;

public interface SteamCrossplatformSyncConfig {
    Path getConfigLocation();

    default Path getLocalCloudSyncLocation() {
        return ConfigLoader.loadIfExists(getConfigLocation())
                .map(ConfigRaw::getLocalSyncLocation)
                .filter(Objects::nonNull)
                .map(Path::of)
                .orElse(CloudSyncLocationSupplier.get(this).orElseThrow());
    }

    // Which folder to write into the cloud storage, relative to the root
    default Path getCloudStorageRelativeWritePath() {
        return ConfigLoader.loadIfExists(getConfigLocation())
                .map(ConfigRaw::getSyncStorageRelativePath)
                .filter(Objects::nonNull)
                .map(Path::of)
                .orElse(Path.of("steam-crossplatform-sync"));
    }

    default Path getGamesFile() {
        return ConfigLoader.loadIfExists(getConfigLocation())
                .map(ConfigRaw::getGamesFileLocation)
                .filter(Objects::nonNull)
                .map(Path::of)
                .orElse(Path.of(getLocalCloudSyncLocation().toAbsolutePath().toString(), "/games.yml"));
    }
}
