package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.GameRunningDetector;
import com.selesse.steam.crossplatform.sync.SteamCrossplatformSyncContext;
import com.selesse.steam.crossplatform.sync.SyncGameFilesService;
import com.selesse.steam.games.SteamGame;
import com.selesse.steam.processes.GameOverlayProcessLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameMonitor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMonitor.class);

    private final SteamCrossplatformSyncContext context;
    private SteamGame runningGame;

    public GameMonitor(SteamCrossplatformSyncContext context) {
        this.context = context;
    }

    @Override
    public void run() {
        synchronized (this) {
            if (GameRunningDetector.isGameCurrentlyRunning()) {
                long currentGameId = GameRunningDetector.getCurrentlyRunningGameId();

                if (runningGame == null) {
                    runningGame = context.loadGame(currentGameId);
                    onGameLaunch(runningGame);
                } else if (currentGameId != runningGame.getId()) {
                    SteamGame newGame = context.loadGame(currentGameId);
                    LOGGER.info(
                            "Game switch detected, closed {} but opened {}", runningGame.getName(), newGame.getName());
                    LOGGER.info("Game closed: {}", runningGame.getName());
                    runningGame = newGame;
                    onGameLaunch(newGame);
                }
            } else if (runningGame != null) {
                LOGGER.info("Game closed: {}", runningGame.getName());
                LOGGER.info("Running sync service for {}", runningGame.getName());
                new SyncGameFilesService(context).run(runningGame);
                runningGame = null;
            }
        }
    }

    private void onGameLaunch(SteamGame runningGame) {
        LOGGER.info("Game launched: {}", runningGame.getName());

        GameOverlayProcessLocator.locate()
                .ifPresentOrElse(
                        processHandle -> processHandle.onExit().thenRunAsync(this),
                        () -> LOGGER.info("Couldn't find game overlay process"));
    }
}
