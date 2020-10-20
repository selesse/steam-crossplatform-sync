package com.selesse.steam.crossplatform.sync.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.io.Resources;
import com.selesse.steam.crossplatform.sync.GameConfig;
import com.selesse.steam.crossplatform.sync.serialize.GameConfigRaw;

import java.io.IOException;
import java.nio.file.Path;

public interface SteamCrossplatformSyncConfig {
    Path getLocalSyncLocation();
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
}
