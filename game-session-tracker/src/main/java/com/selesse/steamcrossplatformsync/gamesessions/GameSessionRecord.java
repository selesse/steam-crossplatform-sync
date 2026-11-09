package com.selesse.steamcrossplatformsync.gamesessions;

import java.time.OffsetDateTime;

/**
 * @param steamAccountId the 64-bit id of the Steam account that played, or {@code null} if it
 *     couldn't be determined
 */
public record GameSessionRecord(
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        long gameId,
        String gameName,
        String hostname,
        long activePlaytimeSeconds,
        Long steamAccountId) {}
