package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.GameRunningDetector;
import com.selesse.steam.SteamAccountId;
import com.selesse.steam.SteamApp;
import com.selesse.steam.crossplatform.sync.SteamCrossplatformSyncContext;
import com.selesse.steam.processes.GameOverlayProcessLocator;
import com.selesse.steamcrossplatformsync.gamesessions.GameSession;
import java.util.Optional;
import java.util.OptionalLong;
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
        // run() is invoked both from a scheduled poll and from a game overlay process's onExit()
        // callback (see startTracking() below) - the latter has no caller able to observe or log
        // a thrown exception, so it must never escape this method.
        try {
            OptionalLong currentGameIdMaybe = GameRunningDetector.getCurrentlyRunningGameId();

            if (currentGameIdMaybe.isPresent()) {
                long currentGameId = currentGameIdMaybe.getAsLong();

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
        } catch (RuntimeException e) {
            LOGGER.warn("Game monitor run failed", e);
        }
    }

    private TrackedGame startTracking(long gameId) {
        Long steamAccountId = findSignedInSteamAccountId();
        TrackedGame tracked;
        try {
            SteamApp game = context.loadGame(gameId);
            tracked = new KnownGame(game, GameSession.start(game.getId(), game.getName(), steamAccountId));
            LOGGER.info("Game launched: {}", game.getName());
        } catch (RuntimeException e) {
            LOGGER.warn("Could not load game {}, tracking session by app ID only", gameId, e);
            tracked = new UnknownGame(gameId, GameSession.start(gameId, null, steamAccountId));
            LOGGER.info("Game launched (app ID: {}, name could not be resolved)", gameId);
            new GameLoadErrorHook(gameId, e).runAsync(context.getConfig());
        }
        GameOverlayProcessLocator.locate()
                .ifPresentOrElse(
                        processHandle -> processHandle.onExit().thenRunAsync(this),
                        () -> LOGGER.info("Couldn't find game overlay process"));
        return tracked;
    }

    /** Best-effort: a session without an account is still worth recording. */
    private Long findSignedInSteamAccountId() {
        try {
            Optional<SteamAccountId> accountId = context.findSignedInSteamAccountId();
            if (accountId.isEmpty()) {
                LOGGER.info("Could not tell which Steam account is signed in");
                return null;
            }
            return accountId.get().to64BitId();
        } catch (RuntimeException e) {
            LOGGER.warn("Could not tell which Steam account is signed in", e);
            return null;
        }
    }
}
