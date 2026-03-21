package com.selesse.steamcrossplatformsync.gamesessions;

import java.time.OffsetDateTime;

public record GameSessionRecord(
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        long gameId,
        String gameName,
        String hostname,
        long activePlaytimeSeconds) {}
