package com.selesse.steam.crossplatform.sync.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.io.Resources;
import com.selesse.steam.crossplatform.sync.GameConfig;
import com.selesse.steam.crossplatform.sync.drive.GoogleDrive;
import com.selesse.steam.crossplatform.sync.serialize.GameConfigRaw;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public interface SteamCrossplatformSyncConfig {
    default Path getLocalSyncLocation() {
        return localConfigMaybe().map(SteamCrossplatformSyncConfig::getLocalSyncLocation)
                .orElse(getDrivePath().orElseThrow());
    };
    Path getConfigLocation();

    @SuppressWarnings("UnstableApiUsage")
    default Path getGamesFile() {
        Path sharedConfig = Path.of(getLocalSyncLocation().toAbsolutePath().toString(), "/games.yml");
        if (sharedConfig.toFile().exists()) {
            return sharedConfig;
        }
        return Path.of(Resources.getResource("games.yml").getFile());
    }

    default GameConfig loadGames() {
        var mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        try {
            return GameConfig.fromRaw(mapper.readValue(getGamesFile().toFile(), GameConfigRaw.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default Optional<SteamCrossplatformSyncConfig> localConfigMaybe() {
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

    private Optional<Path> getDrivePath() {
        return GoogleDrive.getDriveRoot().map(
                config -> Path.of(config.toAbsolutePath().toString(), "gaming", "steam-crossplatform-sync")
        );
    }
}
