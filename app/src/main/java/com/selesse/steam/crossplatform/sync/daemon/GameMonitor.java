package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.GameRunningDetector;
import com.selesse.steam.Games;
import com.selesse.steam.crossplatform.sync.SyncGameFilesService;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steam.steamcmd.SteamGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameMonitor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMonitor.class);

    private final SteamCrossplatformSyncConfig config;
    private boolean gameIsRunning;
    private SteamGame runningGame;

    public GameMonitor(SteamCrossplatformSyncConfig config) {
        this.gameIsRunning = GameRunningDetector.isGameCurrentlyRunning();
        this.config = config;
        if (gameIsRunning) {
            this.runningGame = Games.loadGame(config.getConfigDirectory(), runningGame.getId());
        }
    }

    @Override
    public void run() {
        boolean gameIsRunningRightNow = GameRunningDetector.isGameCurrentlyRunning();

        if (gameIsRunningRightNow != gameIsRunning) {
            if (!gameIsRunningRightNow) {
                LOGGER.info("Game just closed - running sync service for {}", runningGame.getName());
                new SyncGameFilesService(config).run(runningGame.getId());
                runningGame = null;
            } else {
                runningGame = loadGame(GameRunningDetector.getCurrentlyRunningGameId());
                LOGGER.info("{} just launched", runningGame.getName());
            }

            gameIsRunning = gameIsRunningRightNow;
        }
    }

    private SteamGame loadGame(long gameId) {
        return Games.loadGame(config.getConfigDirectory(), gameId);
    }
}
