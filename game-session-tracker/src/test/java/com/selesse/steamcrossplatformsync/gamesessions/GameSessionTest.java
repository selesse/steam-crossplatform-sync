package com.selesse.steamcrossplatformsync.gamesessions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.selesse.steam.games.SteamGame;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.Test;
import org.mockito.Mockito;

public class GameSessionTest {
    private final SteamGame mockGame = createMockGame();

    private static SteamGame createMockGame() {
        SteamGame mock = Mockito.mock(SteamGame.class);
        doReturn("Test Game").when(mock).getName();
        doReturn(12345L).when(mock).getId();
        return mock;
    }

    @Test
    public void recordActiveAccumulatesTimeWithNormalPolling() {
        var startTime = Instant.parse("2024-01-01T12:00:00Z");
        var mutableClock = new MutableClock(startTime);

        var session = GameSession.start(mockGame, mutableClock);

        // Simulate 3 polls at 1-minute intervals
        mutableClock.advance(Duration.ofMinutes(1));
        session.recordActive();

        mutableClock.advance(Duration.ofMinutes(1));
        session.recordActive();

        mutableClock.advance(Duration.ofMinutes(1));
        session.recordActive();

        assertThat(session.getActivePlaytimeSeconds()).isEqualTo(180); // 3 minutes
    }

    @Test
    public void recordActiveIgnoresSuspendTime() {
        var startTime = Instant.parse("2024-01-01T12:00:00Z");
        var mutableClock = new MutableClock(startTime);

        var session = GameSession.start(mockGame, mutableClock);

        // Normal poll after 1 minute
        mutableClock.advance(Duration.ofMinutes(1));
        session.recordActive();

        // Suspend for 30 minutes (simulated by large time jump)
        mutableClock.advance(Duration.ofMinutes(30));
        session.recordActive();

        // Normal poll after 1 minute
        mutableClock.advance(Duration.ofMinutes(1));
        session.recordActive();

        // Should only count 2 minutes (the two normal polls), not 32 minutes
        assertThat(session.getActivePlaytimeSeconds()).isEqualTo(120);
    }

    @Test
    public void recordActiveCountsTimeUpToThreshold() {
        var startTime = Instant.parse("2024-01-01T12:00:00Z");
        var mutableClock = new MutableClock(startTime);

        var session = GameSession.start(mockGame, mutableClock);

        // 2 minutes is at the threshold, should be counted
        mutableClock.advance(Duration.ofMinutes(2));
        session.recordActive();

        assertThat(session.getActivePlaytimeSeconds()).isEqualTo(120);
    }

    @Test
    public void recordActiveIgnoresTimeOverThreshold() {
        var startTime = Instant.parse("2024-01-01T12:00:00Z");
        var mutableClock = new MutableClock(startTime);

        var session = GameSession.start(mockGame, mutableClock);

        // 2 minutes + 1 second is over threshold, should not be counted
        mutableClock.advance(Duration.ofMinutes(2).plusSeconds(1));
        session.recordActive();

        assertThat(session.getActivePlaytimeSeconds()).isEqualTo(0);
    }

    private static class MutableClock extends Clock {
        private Instant instant;
        private final ZoneId zone = ZoneId.of("UTC");

        MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            this.instant = this.instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
