package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.GameRunningDetector;
import com.selesse.steam.crossplatform.sync.SyncGameFilesService;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steam.steamcmd.games.SteamGameMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameMonitor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMonitor.class);

    private final SteamCrossplatformSyncConfig config;
    private boolean gameIsRunning;
    private SteamGameMetadata runningGameMetadata;

    public GameMonitor(SteamCrossplatformSyncConfig config) {
        this.gameIsRunning = GameRunningDetector.isGameCurrentlyRunning();
        if (gameIsRunning) {
            this.runningGameMetadata = GameRunningDetector.getCurrentlyRunningGameMetadata();
        }
        this.config = config;
    }

    @Override
    public void run() {
        boolean gameIsRunningRightNow = GameRunningDetector.isGameCurrentlyRunning();

        if (gameIsRunningRightNow != gameIsRunning) {
            if (!gameIsRunningRightNow) {
                LOGGER.info("Game just closed - running sync service for {}", runningGameMetadata.getName());
                new SyncGameFilesService(config).run(runningGameMetadata.getGameId());
            } else {
                runningGameMetadata = GameRunningDetector.getCurrentlyRunningGameMetadata();
                LOGGER.info("{} just launched", runningGameMetadata.getName());
            }

            gameIsRunning = gameIsRunningRightNow;
        }
    }
}
