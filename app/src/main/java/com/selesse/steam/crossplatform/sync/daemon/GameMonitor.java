package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.GameRunningDetector;
import com.selesse.steam.crossplatform.sync.SteamCrossplatformSyncContext;
import com.selesse.steam.crossplatform.sync.SyncGameFilesService;
import com.selesse.steam.games.SteamGame;
import com.selesse.steam.processes.GameOverlayProcessLocator;
import com.selesse.steamcrossplatformsync.gamesessions.GameSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameMonitor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMonitor.class);

    private final SteamCrossplatformSyncContext context;
    private SteamGame runningGame;
    private GameSession gameSession;
    private long unknownGameId = -1;

    public GameMonitor(SteamCrossplatformSyncContext context) {
        this.context = context;
    }

    @Override
    public void run() {
        synchronized (this) {
            if (GameRunningDetector.isGameCurrentlyRunning()) {
                long currentGameId = GameRunningDetector.getCurrentlyRunningGameId();

                if (runningGame == null) {
                    if (unknownGameId == currentGameId) {
                        gameSession.recordActive();
                    } else {
                        try {
                            runningGame = context.loadGame(currentGameId);
                            unknownGameId = -1;
                            onGameLaunch(runningGame);
                        } catch (RuntimeException e) {
                            LOGGER.warn("Could not load game {}, tracking session by app ID only", currentGameId, e);
                            unknownGameId = currentGameId;
                            onUnknownGameLaunch(currentGameId);
                        }
                    }
                } else if (currentGameId != runningGame.getId()) {
                    SteamGame newGame = context.loadGame(currentGameId);
                    LOGGER.info(
                            "Game switch detected, closed {} but opened {}", runningGame.getName(), newGame.getName());
                    onGameClosed(runningGame);
                    runningGame = newGame;
                    onGameLaunch(newGame);
                } else {
                    gameSession.recordActive();
                }
            } else if (runningGame != null) {
                onGameClosed(runningGame);
                runningGame = null;
            } else if (unknownGameId != -1) {
                onUnknownGameClosed();
            }
        }
    }

    private void onGameLaunch(SteamGame runningGame) {
        gameSession = GameSession.start(runningGame);
        LOGGER.info("Game launched: {}", runningGame.getName());

        GameOverlayProcessLocator.locate()
                .ifPresentOrElse(
                        processHandle -> processHandle.onExit().thenRunAsync(this),
                        () -> LOGGER.info("Couldn't find game overlay process"));
    }

    private void onUnknownGameLaunch(long gameId) {
        gameSession = GameSession.start(gameId, null);
        LOGGER.info("Game launched (app ID: {}, name could not be resolved)", gameId);

        GameOverlayProcessLocator.locate()
                .ifPresentOrElse(
                        processHandle -> processHandle.onExit().thenRunAsync(this),
                        () -> LOGGER.info("Couldn't find game overlay process"));
    }

    private void onGameClosed(SteamGame runningGame) {
        gameSession.finish();
        LOGGER.info("Game closed: {}", runningGame.getName());
        LOGGER.info("Running sync service for {}", runningGame.getName());
        new SyncGameFilesService(context).run(runningGame);
    }

    private void onUnknownGameClosed() {
        gameSession.finish();
        LOGGER.info("Game closed (app ID: {}, name could not be resolved)", unknownGameId);
        gameSession = null;
        unknownGameId = -1;
    }
}
