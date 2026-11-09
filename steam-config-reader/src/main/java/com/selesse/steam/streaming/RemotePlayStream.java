package com.selesse.steam.streaming;

import java.time.OffsetDateTime;

/**
 * One Remote Play stream, as this machine's Steam logged it.
 *
 * @param role whether this machine ran the game ({@link Role#HOST}) or showed it ({@link
 *     Role#CLIENT})
 * @param remoteHost the other end: a machine name when streaming between your own machines, a
 *     friend's persona name for Remote Play Together. Only the host logs it, so {@code null} for a
 *     client.
 * @param appId the game streamed, or {@code null} if the log never named one
 * @param remotePlayTogether whether this was Remote Play Together, or {@code null} if the log never
 *     said. Only the host logs it.
 */
public record RemotePlayStream(
        Role role,
        String remoteHost,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        Long appId,
        Boolean remotePlayTogether) {
    public enum Role {
        HOST,
        CLIENT
    }
}
