package com.selesse.steamcrossplatformsync.gamesessions;

import com.selesse.os.Hostnames;
import com.selesse.steam.games.SteamGame;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;

public class GameSession {
    // 2x the normal daemon polling interval (1 minute) to allow for timing variance.
    // If elapsed time exceeds this, we assume a suspend/sleep occurred.
    // See: Daemon.java for polling configuration.
    private static final Duration MAX_EXPECTED_POLL_INTERVAL = Duration.ofMinutes(2);

    private final long gameId;
    private final String gameName;
    private final Clock clock;
    private final OffsetDateTime startedAt;
    private long activePlaytimeSeconds;
    private OffsetDateTime lastActiveAt;

    private GameSession(long gameId, String gameName, Clock clock) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.clock = clock;
        this.startedAt = OffsetDateTime.now(clock);
        this.lastActiveAt = this.startedAt;
        this.activePlaytimeSeconds = 0;
    }

    public static GameSession start(SteamGame steamGame) {
        return start(steamGame.getId(), steamGame.getName());
    }

    public static GameSession start(long gameId, String gameName) {
        return new GameSession(gameId, gameName, Clock.systemDefaultZone());
    }

    static GameSession start(long gameId, String gameName, Clock clock) {
        return new GameSession(gameId, gameName, clock);
    }

    public void recordActive() {
        var now = OffsetDateTime.now(clock);
        var elapsed = Duration.between(lastActiveAt, now);

        // Only count time if it looks like normal polling (not a suspend/sleep)
        if (elapsed.compareTo(MAX_EXPECTED_POLL_INTERVAL) <= 0) {
            activePlaytimeSeconds += elapsed.getSeconds();
        }
        lastActiveAt = now;
    }

    long getActivePlaytimeSeconds() {
        return activePlaytimeSeconds;
    }

    public GameSessionRecord finish() {
        recordActive();
        var finishedAt = OffsetDateTime.now(clock);

        var record = new GameSessionRecord(
                startedAt, finishedAt, gameId, gameName, Hostnames.getCurrent(), activePlaytimeSeconds);
        GameSessionRepository.getInstance().save(record);
        return record;
    }
}
