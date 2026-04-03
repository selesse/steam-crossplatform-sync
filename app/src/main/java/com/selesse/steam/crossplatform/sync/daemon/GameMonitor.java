package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.GameRunningDetector;
import com.selesse.steam.crossplatform.sync.SteamCrossplatformSyncContext;
import com.selesse.steam.games.SteamGame;
import com.selesse.steam.processes.GameOverlayProcessLocator;
import com.selesse.steamcrossplatformsync.gamesessions.GameSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameMonitor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMonitor.class);

    private final SteamCrossplatformSyncContext context;
    private TrackedGame currentGame = null;

    public GameMonitor(SteamCrossplatformSyncContext context) {
        this.context = context;
    }

    @Override
    public synchronized void run() {
        if (GameRunningDetector.isGameCurrentlyRunning()) {
            long currentGameId = GameRunningDetector.getCurrentlyRunningGameId();

            if (currentGame == null) {
                currentGame = startTracking(currentGameId);
            } else if (currentGameId != currentGame.getId()) {
                TrackedGame closing = currentGame;
                currentGame = startTracking(currentGameId);
                closing.onClosed(context);
            } else {
                currentGame.session().recordActive();
            }
        } else if (currentGame != null) {
            TrackedGame closing = currentGame;
            currentGame = null;
            closing.onClosed(context);
        }
    }

    private TrackedGame startTracking(long gameId) {
        TrackedGame tracked;
        try {
            SteamGame game = context.loadGame(gameId);
            tracked = new KnownGame(game, GameSession.start(game));
            LOGGER.info("Game launched: {}", game.getName());
        } catch (RuntimeException e) {
            LOGGER.warn("Could not load game {}, tracking session by app ID only", gameId, e);
            tracked = new UnknownGame(gameId, GameSession.start(gameId, null));
            LOGGER.info("Game launched (app ID: {}, name could not be resolved)", gameId);
            new GameLoadErrorHook(gameId, e).runAsync(context.getConfig());
        }
        GameOverlayProcessLocator.locate()
                .ifPresentOrElse(
                        processHandle -> processHandle.onExit().thenRunAsync(this),
                        () -> LOGGER.info("Couldn't find game overlay process"));
        return tracked;
    }
}
