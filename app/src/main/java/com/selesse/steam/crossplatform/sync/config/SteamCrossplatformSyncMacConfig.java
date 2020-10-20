package com.selesse.steam.crossplatform.sync.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.selesse.steam.crossplatform.sync.drive.GoogleDrive;

import java.nio.file.Path;
import java.util.Optional;

public class SteamCrossplatformSyncMacConfig implements SteamCrossplatformSyncConfig {
    @Override
    public Path getLocalSyncLocation() {
        return localConfigMaybe().map(SteamCrossplatformSyncConfig::getLocalSyncLocation)
                .orElse(getDrivePath().orElseThrow());
    }

    private Optional<SteamCrossplatformSyncConfig> localConfigMaybe() {
        var mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        try {
            // e.g. read the local config, otherwise pick a sane default
            SteamCrossplatformSyncConfig config =
                    mapper.readValue(getConfigLocation().toFile(), SteamCrossplatformSyncConfig.class);
            return Optional.ofNullable(config);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Path getConfigLocation() {
        return Path.of(System.getProperty("user.home"), ".config", "steamcrossplatformsync", "config");
    }

    private Optional<Path> getDrivePath() {
        return GoogleDrive.getDriveRoot().map(
                config -> Path.of(config.toAbsolutePath().toString(), "gaming", "steam-crossplatform-sync")
        );
    }

}
