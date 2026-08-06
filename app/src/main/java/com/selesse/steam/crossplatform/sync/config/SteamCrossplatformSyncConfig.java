package com.selesse.steam.crossplatform.sync.config;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.crossplatform.sync.cloud.CloudSyncLocationSupplier;
import com.selesse.steam.crossplatform.sync.serialize.ConfigRaw;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;

public class SteamCrossplatformSyncConfig {
    private final Path configDirectory;
    private final @Nullable ConfigRaw raw;
    private final CloudSyncLocationSupplier cloudSyncLocations;

    /** Reads this machine's config file, once. */
    public static SteamCrossplatformSyncConfig load() {
        Path configDirectory = defaultConfigDirectory();
        return new SteamCrossplatformSyncConfig(
                configDirectory,
                ConfigLoader.read(configFileIn(configDirectory)).orElse(null),
                new CloudSyncLocationSupplier());
    }

    @VisibleForTesting
    public SteamCrossplatformSyncConfig(
            Path configDirectory, @Nullable ConfigRaw raw, CloudSyncLocationSupplier cloudSyncLocations) {
        this.configDirectory = configDirectory;
        this.raw = raw;
        this.cloudSyncLocations = cloudSyncLocations;
    }

    private static Path defaultConfigDirectory() {
        return switch (OperatingSystems.get().family()) {
            case WINDOWS -> Path.of(System.getenv("LOCALAPPDATA"), "steam-crossplatform-sync");
            case MAC, LINUX -> {
                String xdgConfigHome = Optional.ofNullable(System.getenv("XDG_CONFIG_HOME"))
                        .orElse(System.getProperty("user.home") + "/.config");
                yield Path.of(xdgConfigHome, "steam-crossplatform-sync");
            }
        };
    }

    private static Path configFileIn(Path configDirectory) {
        return Path.of(configDirectory.toAbsolutePath().toString(), "config.yml");
    }

    public Path getConfigDirectory() {
        return configDirectory;
    }

    public Path getConfigFileLocation() {
        return configFileIn(configDirectory);
    }

    /**
     * @return The absolute path to where we should be syncing games config files to, which
     * incorporates {@link #getCloudStorageRelativeWritePath()}}
     */
    public Path getLocalCloudSyncBaseDirectory() {
        return configValue(ConfigRaw::getPathToCloudStorage).map(Path::of).orElseGet(() -> cloudSyncLocations
                .get(getCloudProvider(), getCloudStorageRelativeWritePath())
                .orElseThrow(() ->
                        new IllegalStateException("Could not find a cloud storage location. Set pathToCloudStorage in "
                                + getConfigFileLocation().toAbsolutePath()
                                + ", or install a supported cloud storage provider.")));
    }

    // Which folder to write into the cloud storage, relative to the root
    public Path getCloudStorageRelativeWritePath() {
        return configValue(ConfigRaw::getCloudStorageRelativeWritePath)
                .map(Path::of)
                .orElse(Path.of("steam-crossplatform-sync"));
    }

    public Path getGamesFile() {
        return configValue(ConfigRaw::getGamesFileLocation)
                .map(Path::of)
                .orElseGet(() -> Path.of(
                        getLocalCloudSyncBaseDirectory().toAbsolutePath().toString(), "/games.yml"));
    }

    public @Nullable String getCloudProvider() {
        return configValue(r -> r.cloudProvider).orElse(null);
    }

    private Optional<String> configValue(Function<ConfigRaw, String> getter) {
        return Optional.ofNullable(raw).map(getter).filter(x -> !x.isEmpty());
    }
}
