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

    private final SteamGame game;
    private final Clock clock;
    private final OffsetDateTime startedAt;
    private long activePlaytimeSeconds;
    private OffsetDateTime lastActiveAt;

    private GameSession(SteamGame steamGame, Clock clock) {
        this.game = steamGame;
        this.clock = clock;
        this.startedAt = OffsetDateTime.now(clock);
        this.lastActiveAt = this.startedAt;
        this.activePlaytimeSeconds = 0;
    }

    public static GameSession start(SteamGame steamGame) {
        return new GameSession(steamGame, Clock.systemDefaultZone());
    }

    static GameSession start(SteamGame steamGame, Clock clock) {
        return new GameSession(steamGame, clock);
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

    public void finish() {
        recordActive();
        var finishedAt = OffsetDateTime.now(clock);

        GameSessionRepository.getInstance()
                .save(new GameSessionRecord(
                        startedAt, finishedAt, game, Hostnames.getCurrent(), activePlaytimeSeconds));
    }
}
