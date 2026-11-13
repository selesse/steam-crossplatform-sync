package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.GameRunningDetector;
import com.selesse.steam.SteamAccountId;
import com.selesse.steam.SteamApp;
import com.selesse.steam.crossplatform.sync.SteamCrossplatformSyncContext;
import com.selesse.steam.processes.GameOverlayProcessLocator;
import com.selesse.steamcrossplatformsync.gamesessions.GameSession;
import java.util.Optional;
import java.util.OptionalLong;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Follows which game is running, one poll at a time, and closes out each session when its game
 * stops.
 *
 * <pre>
 * From        Event              Transition       To          Effects
 * ----------  -----------------  ---------------  ----------  ---------------------------------------
 * Idle        poll: nothing      NOTHING_RUNNING  Idle        -
 * Idle        poll: game A       LAUNCHED         Playing(A)  start A's session
 * Playing(A)  poll: game A       STILL_PLAYING    Playing(A)  count the time since the last poll
 * Playing(A)  poll: game B       SWITCHED         Playing(B)  close A (record, sync, hook); start B
 * Playing(A)  poll: nothing      CLOSED           Idle        close A (record, sync, hook)
 * any         shutdown()         -                Stopped     after a last poll, record A's session
 *                                                             if still playing (no sync, no hook)
 * Stopped     poll               -                Stopped     -
 * </pre>
 *
 * <p>Every transition happens under this object's lock, so a scheduled poll, an overlay-exit
 * callback and the shutdown hook can never interleave.
 */
public class GameMonitor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMonitor.class);

    sealed interface State {
        record Idle() implements State {}

        record Playing(TrackedGame game) implements State {}

        record Stopped() implements State {}
    }

    /** What a poll saw, relative to what was being tracked. */
    enum Transition {
        NOTHING_RUNNING,
        LAUNCHED,
        STILL_PLAYING,
        SWITCHED,
        CLOSED;

        /** Pure: which transition a poll makes, given the tracked game (if any) and the running one. */
        static Transition between(OptionalLong trackedId, OptionalLong runningId) {
            if (trackedId.isEmpty()) {
                return runningId.isPresent() ? LAUNCHED : NOTHING_RUNNING;
            }
            if (runningId.isEmpty()) {
                return CLOSED;
            }
            return runningId.getAsLong() == trackedId.getAsLong() ? STILL_PLAYING : SWITCHED;
        }
    }

    private final SteamCrossplatformSyncContext context;
    private State state = new State.Idle();

    public GameMonitor(SteamCrossplatformSyncContext context) {
        this.context = context;
    }

    /** Polls for the running game and applies whichever transition that implies. */
    @Override
    public synchronized void run() {
        // run() is invoked both from a scheduled poll and from a game overlay process's onExit()
        // callback (see startTracking() below) - the latter has no caller able to observe or log
        // a thrown exception, so it must never escape this method.
        try {
            if (state instanceof State.Stopped) {
                return;
            }
            poll(GameRunningDetector.getCurrentlyRunningGameId());
        } catch (RuntimeException e) {
            LOGGER.warn("Game monitor run failed", e);
        }
    }

    /**
     * Stops monitoring for good. A game that has closed is handled as usual; one that is still
     * running has its session recorded so far, without a sync or the session-end hook, since its
     * saves may be mid-write and the hook couldn't outlive the shutdown anyway. Its next session
     * starts when the daemon does.
     */
    public synchronized void shutdown() {
        run();
        if (state instanceof State.Playing(TrackedGame game)) {
            LOGGER.info("Recording the session of game {}, still running at shutdown", game.getId());
            try {
                game.finishSession(context);
            } catch (RuntimeException e) {
                LOGGER.warn("Could not record the session of game {} at shutdown", game.getId(), e);
            }
        }
        state = new State.Stopped();
    }

    @VisibleForTesting
    synchronized State state() {
        return state;
    }

    private void poll(OptionalLong runningId) {
        TrackedGame tracked = state instanceof State.Playing(TrackedGame game) ? game : null;
        OptionalLong trackedId = tracked == null ? OptionalLong.empty() : OptionalLong.of(tracked.getId());

        switch (Transition.between(trackedId, runningId)) {
            case NOTHING_RUNNING -> {}
            case LAUNCHED -> state = new State.Playing(startTracking(runningId.getAsLong()));
            case STILL_PLAYING -> tracked.session().recordActive();
            case SWITCHED -> {
                // The new game is tracked before the old one's (slow) sync runs, so it isn't
                // missing from the state if closing out the old one throws.
                state = new State.Playing(startTracking(runningId.getAsLong()));
                tracked.onClosed(context);
            }
            case CLOSED -> {
                state = new State.Idle();
                tracked.onClosed(context);
            }
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
