package com.selesse.steam.crossplatform.sync.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.selesse.steam.crossplatform.sync.GameConfig;
import com.selesse.steam.crossplatform.sync.serialize.GameConfigRaw;
import java.io.IOException;

public class GameLoader {
    public GameConfig loadGames(SteamCrossplatformSyncConfig config) {
        var mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        try {
            return GameConfig.fromRaw(mapper.readValue(config.getGamesFile().toFile(), GameConfigRaw.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
