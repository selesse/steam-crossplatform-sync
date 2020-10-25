package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.GameRunningDetector;
import com.selesse.steam.crossplatform.sync.SyncGameFilesService;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameMonitor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMonitor.class);

    private final SteamCrossplatformSyncConfig config;
    private boolean gameIsRunning;

    public GameMonitor(SteamCrossplatformSyncConfig config) {
        this.gameIsRunning = GameRunningDetector.isGameCurrentlyRunning();
        this.config = config;
    }

    @Override
    public void run() {
        boolean gameIsRunningRightNow = GameRunningDetector.isGameCurrentlyRunning();

        if (gameIsRunningRightNow != gameIsRunning) {
            if (!gameIsRunningRightNow) {
                LOGGER.info("Game just closed - running sync service");
                new SyncGameFilesService(config).run();
            } else {
                LOGGER.info("Game just launched");
            }

            gameIsRunning = gameIsRunningRightNow;
        }
    }
}
