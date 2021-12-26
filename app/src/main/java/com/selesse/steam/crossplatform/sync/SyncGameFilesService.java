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
        gameList.getGames().forEach(this::sync);
    }

    public void run(long runningGameId) {
        run(new Long[] { runningGameId });
    }

    public void run(Long[] gameIds) {
        List<Long> gamesToSync = Arrays.stream(gameIds).collect(Collectors.toList());
        GameConfig gameList = new GameLoader().loadGames(config);
        for (Long gameId : gamesToSync) {
            gameList.getGame(gameId).ifPresentOrElse(
                    this::sync,
                    () -> LOGGER.warn("Could not find game config for game with ID {}", gameId)
            );
        }
    }

    private void sync(SyncableGame game) {
        if (game.isSupportedOnThisOs()) {
            Path localPath = game.getLocalPath();
            Path cloudSyncPath = game.getLocalCloudSyncPath(config);

            LOGGER.info("Checking {}", game.getName());
            GameSyncer.sync(localPath, cloudSyncPath);
        } else {
            LOGGER.info("Did not check {} because it is unsupported on this OS", game.getName());
        }
    }
}
