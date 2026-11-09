package com.selesse.steam.streaming;

import com.selesse.steam.streaming.RemotePlayStream.Role;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds Remote Play streams in Steam's logs. Neither log format is documented; these rules come
 * from logs written by real streams, between a user's own machines and with friends.
 *
 * <p>Steam stamps each line with local time and no offset, so the caller says which zone wrote it.
 * A stream with no recorded end yet is left out: it is either still going or will be closed off by
 * a later line, and the next read picks it up.
 */
public final class RemotePlayLogParser {
    private static final Pattern TIMESTAMP = Pattern.compile("^\\[(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})]");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Pattern HOST_STARTED = Pattern.compile("] Streaming started to (.+) at \\S+, ");
    private static final String HOST_STOPPED = ">>> Stopped desktop stream";
    private static final String STEAM_STARTED = "] Client version: ";
    private static final Pattern APP_ID = Pattern.compile("^\\s*\"appid\"\\s+\"(\\d+)\"");
    private static final Pattern REMOTE_PLAY_TOGETHER = Pattern.compile("^\\s*\"RemotePlayTogether\"\\s+\"([01])\"");

    private static final Pattern CLIENT_STARTED = Pattern.compile("StreamingClientStarted for appid (\\d+)");
    private static final Pattern CLIENT_FINISHED = Pattern.compile("StreamingClientFinished for appid (\\d+)");

    private RemotePlayLogParser() {}

    /**
     * Streams this machine sent, from {@code streaming_log.txt}.
     *
     * <p>A stream captured from the desktop ends with {@code >>> Stopped desktop stream}. One
     * captured from a game's overlay can end without saying so; it can't outlive Steam, so a Steam
     * restart or the next stream closes it at the last line logged before.
     */
    public static List<RemotePlayStream> hostStreams(List<String> lines, ZoneId zone) {
        List<RemotePlayStream> streams = new ArrayList<>();
        HostStream open = null;
        OffsetDateTime lastTimestamp = null;

        for (String line : lines) {
            OffsetDateTime timestamp = timestamp(line, zone);
            Matcher started = HOST_STARTED.matcher(line);

            if (timestamp != null && started.find()) {
                if (open != null) {
                    streams.add(open.finish(lastTimestamp));
                }
                open = new HostStream(started.group(1), timestamp);
            } else if (timestamp != null && line.contains(STEAM_STARTED)) {
                if (open != null) {
                    streams.add(open.finish(lastTimestamp));
                    open = null;
                }
            } else if (open != null) {
                if (timestamp != null && line.contains(HOST_STOPPED)) {
                    streams.add(open.finish(timestamp));
                    open = null;
                } else {
                    open.read(line);
                }
            }

            if (timestamp != null) {
                lastTimestamp = timestamp;
            }
        }
        return streams;
    }

    /** Streams this machine received, from {@code webhelper_js.txt}. */
    public static List<RemotePlayStream> clientStreams(List<String> lines, ZoneId zone) {
        List<RemotePlayStream> streams = new ArrayList<>();
        OffsetDateTime openedAt = null;
        Long openAppId = null;

        for (String line : lines) {
            OffsetDateTime timestamp = timestamp(line, zone);
            if (timestamp == null) {
                continue;
            }
            Matcher started = CLIENT_STARTED.matcher(line);
            Matcher finished = CLIENT_FINISHED.matcher(line);
            if (started.find()) {
                openedAt = timestamp;
                openAppId = appId(started.group(1));
            } else if (finished.find() && openedAt != null) {
                Long appId = openAppId != null ? openAppId : appId(finished.group(1));
                streams.add(new RemotePlayStream(Role.CLIENT, null, openedAt, timestamp, appId, null));
                openedAt = null;
                openAppId = null;
            }
        }
        return streams;
    }

    private static OffsetDateTime timestamp(String line, ZoneId zone) {
        Matcher matcher = TIMESTAMP.matcher(line);
        if (!matcher.find()) {
            return null;
        }
        return LocalDateTime.parse(matcher.group(1), TIMESTAMP_FORMAT)
                .atZone(zone)
                .toOffsetDateTime();
    }

    private static Long appId(String digits) {
        long appId = Long.parseLong(digits);
        return appId > 0 ? appId : null;
    }

    private static final class HostStream {
        private final String remoteHost;
        private final OffsetDateTime startedAt;
        private Long appId;
        private Boolean remotePlayTogether;

        private HostStream(String remoteHost, OffsetDateTime startedAt) {
            this.remoteHost = remoteHost;
            this.startedAt = startedAt;
        }

        // Steam writes a SessionStats block every so often while streaming; the first one to name
        // a game says what was streamed.
        private void read(String line) {
            Matcher appIdMatcher = APP_ID.matcher(line);
            if (appId == null && appIdMatcher.find()) {
                appId = appId(appIdMatcher.group(1));
                return;
            }
            Matcher together = REMOTE_PLAY_TOGETHER.matcher(line);
            if (remotePlayTogether == null && together.find()) {
                remotePlayTogether = together.group(1).equals("1");
            }
        }

        private RemotePlayStream finish(OffsetDateTime finishedAt) {
            return new RemotePlayStream(Role.HOST, remoteHost, startedAt, finishedAt, appId, remotePlayTogether);
        }
    }
}
