package com.selesse.steamcrossplatformsync.gamesessions;

import java.time.OffsetDateTime;

/**
 * A Remote Play stream this machine took part in.
 *
 * @param host this machine
 * @param remoteHost the other end, or {@code null} when this machine's logs don't name it
 * @param steamAppId the game streamed, or {@code null} if unknown
 * @param remotePlayTogether whether it was Remote Play Together, or {@code null} if unknown
 */
public record RemotePlaySessionRecord(
        String host,
        Role role,
        String remoteHost,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        Long steamAppId,
        Boolean remotePlayTogether) {
    public enum Role {
        HOST("host"),
        CLIENT("client");

        private final String databaseValue;

        Role(String databaseValue) {
            this.databaseValue = databaseValue;
        }

        String databaseValue() {
            return databaseValue;
        }
    }
}
