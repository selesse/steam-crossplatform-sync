package com.selesse.steam.crossplatform.sync.config;

import com.selesse.steam.crossplatform.sync.cloud.CloudSyncLocationSupplier;
import com.selesse.steam.crossplatform.sync.serialize.ConfigRaw;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

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
        return configValue(ConfigRaw::getPathToCloudStorage)
                .map(Path::of)
                .orElseGet(() -> CloudSyncLocationSupplier.get(this)
                        .orElseThrow(() -> new IllegalStateException(
                                "Could not find a cloud storage location. Set pathToCloudStorage in "
                                        + getConfigFileLocation().toAbsolutePath()
                                        + ", or install a supported cloud storage provider.")));
    }

    // Which folder to write into the cloud storage, relative to the root
    default Path getCloudStorageRelativeWritePath() {
        return configValue(ConfigRaw::getCloudStorageRelativeWritePath)
                .map(Path::of)
                .orElse(Path.of("steam-crossplatform-sync"));
    }

    default Path getGamesFile() {
        return configValue(ConfigRaw::getGamesFileLocation)
                .map(Path::of)
                .orElseGet(() -> Path.of(
                        getLocalCloudSyncBaseDirectory().toAbsolutePath().toString(), "/games.yml"));
    }

    @Nullable
    default String getCloudProvider() {
        return configValue(r -> r.cloudProvider).orElse(null);
    }

    private Optional<String> configValue(Function<ConfigRaw, String> getter) {
        return ConfigLoader.loadIfExists(getConfigFileLocation()).map(getter).filter(x -> !x.isEmpty());
    }
}
