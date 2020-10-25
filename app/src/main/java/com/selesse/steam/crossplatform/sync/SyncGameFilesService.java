package com.selesse.steam.crossplatform.sync;

import com.selesse.steam.crossplatform.sync.config.GameLoader;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class SyncGameFilesService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncGameFilesService.class);
    private final SteamCrossplatformSyncConfig config;

    public SyncGameFilesService(SteamCrossplatformSyncConfig config) {
        this.config = config;
    }

    public void run() {
        GameConfig gameList = new GameLoader().loadGames(config);
        gameList.getGames().forEach(game -> {
            Path localPath = game.getLocalPath();
            Path cloudSyncPath = game.getLocalCloudSyncPath(config);

            LOGGER.info("Checking {}", game.getName());
            GameSyncer.sync(localPath, cloudSyncPath);
        });
    }
}
