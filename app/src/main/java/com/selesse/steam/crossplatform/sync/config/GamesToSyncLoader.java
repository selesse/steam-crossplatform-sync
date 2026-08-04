package com.selesse.steam.crossplatform.sync.config;

import com.selesse.steam.crossplatform.sync.GameConfig;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

public class GamesToSyncLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(GamesToSyncLoader.class);

    public GameConfig loadGames(SteamCrossplatformSyncConfig config) {
        // getGamesFile() can itself block (e.g. resolving a cloud-storage provider root), so
        // resolve it once, log it, and reuse it - calling it twice would both double that cost
        // and make the bracketing log lines below lie about what's actually blocking.
        Path gamesFile = config.getGamesFile();
        LOGGER.info("Loading games config from {}", gamesFile);
        var mapper = new ObjectMapper(new YAMLFactory());
        GameConfig gameList = mapper.readValue(gamesFile.toFile(), GameConfig.class);
        LOGGER.info("Loaded games config ({} games)", gameList.games().size());
        return gameList;
    }
}
