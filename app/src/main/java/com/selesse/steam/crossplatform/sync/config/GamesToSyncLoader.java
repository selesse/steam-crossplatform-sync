package com.selesse.steam.crossplatform.sync.config;

import com.selesse.steam.crossplatform.sync.GameConfig;
import com.selesse.steam.crossplatform.sync.serialize.GameConfigRaw;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

public class GamesToSyncLoader {
    public GameConfig loadGames(SteamCrossplatformSyncConfig config) {
        var mapper = new ObjectMapper(new YAMLFactory());
        return GameConfig.fromRaw(mapper.readValue(config.getGamesFile().toFile(), GameConfigRaw.class));
    }
}
