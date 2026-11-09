package com.selesse.steamcrossplatformsync.gamesessions;

import com.selesse.os.Hostnames;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;

public class GameSession {
    // Comfortably above the daemon's 30 second polling interval, to allow for timing variance.
    // If elapsed time exceeds this, we assume a suspend/sleep occurred.
    // See: Daemon.java for polling configuration.
    private static final Duration MAX_EXPECTED_POLL_INTERVAL = Duration.ofMinutes(2);

    private final long gameId;
    private final String gameName;
    private final Long steamAccountId;
    private final Clock clock;
    private final OffsetDateTime startedAt;
    private Duration activePlaytime;
    private OffsetDateTime lastActiveAt;

    private GameSession(long gameId, String gameName, Long steamAccountId, Clock clock) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.steamAccountId = steamAccountId;
        this.clock = clock;
        this.startedAt = OffsetDateTime.now(clock);
        this.lastActiveAt = this.startedAt;
        this.activePlaytime = Duration.ZERO;
    }

    /**
     * @param steamAccountId the 64-bit id of the Steam account playing, or {@code null} if unknown.
     *     Taken at the start: whoever launched the game is who played it.
     */
    public static GameSession start(long gameId, String gameName, Long steamAccountId) {
        return new GameSession(gameId, gameName, steamAccountId, Clock.systemDefaultZone());
    }

    static GameSession start(long gameId, String gameName, Long steamAccountId, Clock clock) {
        return new GameSession(gameId, gameName, steamAccountId, clock);
    }

    public void recordActive() {
        var now = OffsetDateTime.now(clock);
        var elapsed = Duration.between(lastActiveAt, now);

        // Only count time if it looks like normal polling (not a suspend/sleep)
        if (elapsed.compareTo(MAX_EXPECTED_POLL_INTERVAL) <= 0) {
            activePlaytime = activePlaytime.plus(elapsed);
        }
        lastActiveAt = now;
    }

    long getActivePlaytimeSeconds() {
        return activePlaytime.getSeconds();
    }

    /** Closes out the session and returns what happened. Persisting it is the caller's business. */
    public GameSessionRecord finish() {
        recordActive();
        var finishedAt = OffsetDateTime.now(clock);

        return new GameSessionRecord(
                startedAt,
                finishedAt,
                gameId,
                gameName,
                Hostnames.getCurrent(),
                getActivePlaytimeSeconds(),
                steamAccountId);
    }
}
