package com.selesse.steam.crossplatform.sync;

import com.selesse.steam.crossplatform.sync.config.GameLoader;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SyncGameFilesService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncGameFilesService.class);
    private final SteamCrossplatformSyncConfig config;

    public SyncGameFilesService(SteamCrossplatformSyncConfig config) {
        this.config = config;
    }

    public void runForAllGames() {
        GameConfig gameList = new GameLoader().loadGames(config);
        gameList.getGames().forEach(game -> {
            Path localPath = game.getLocalPath();
            Path cloudSyncPath = game.getLocalCloudSyncPath(config);

            LOGGER.info("Checking {}", game.getName());
            GameSyncer.sync(localPath, cloudSyncPath);
        });
    }

    public void run(long runningGameId) {
        run(new Long[] { runningGameId });
    }

    public void run(Long[] gameIds) {
        List<Long> gamesToSync = Arrays.stream(gameIds).collect(Collectors.toList());
        GameConfig gameList = new GameLoader().loadGames(config);
        gameList.getGames().stream()
                .filter(game -> gamesToSync.contains(game.getGameId()))
                .forEach(game -> {
                    Path localPath = game.getLocalPath();
                    Path cloudSyncPath = game.getLocalCloudSyncPath(config);
                    GameSyncer.sync(localPath, cloudSyncPath);
                });
    }
}
