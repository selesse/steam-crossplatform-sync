package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steamcrossplatformsync.gamesessions.GameSessionRecord;
import com.selesse.steamcrossplatformsync.gamesessions.database.SqliteDatabaseLocation;
import java.time.Duration;
import java.util.Map;

class SessionEndHook extends Hook {
    private final GameSessionRecord record;

    SessionEndHook(GameSessionRecord record) {
        this.record = record;
    }

    @Override
    String name() {
        return "session-end";
    }

    @Override
    Map<String, String> env() {
        long durationSeconds =
                Duration.between(record.startedAt(), record.finishedAt()).getSeconds();
        return Map.of(
                "STEAM_APP_ID", String.valueOf(record.gameId()),
                "GAME_NAME", record.gameName() != null ? record.gameName() : "",
                "SESSION_STARTED_AT", record.startedAt().toString(),
                "SESSION_ENDED_AT", record.finishedAt().toString(),
                "SESSION_DURATION_SECONDS", String.valueOf(durationSeconds),
                "ACTIVE_PLAYTIME_SECONDS", String.valueOf(record.activePlaytimeSeconds()),
                "DB_PATH", SqliteDatabaseLocation.get().path().toAbsolutePath().toString());
    }
}
