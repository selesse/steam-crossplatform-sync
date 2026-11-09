package com.selesse.steam.streaming;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.steam.streaming.RemotePlayStream.Role;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.Test;

public class RemotePlayLogParserTest {
    private static final ZoneId TORONTO = ZoneId.of("America/Toronto");

    @Test
    public void hostStreamsBetweenOwnMachinesEndWhenTheDesktopStreamStops() throws IOException {
        List<RemotePlayStream> streams =
                RemotePlayLogParser.hostStreams(fixture("streaming_log_own_machines.txt"), TORONTO);

        assertThat(streams)
                .containsExactly(
                        new RemotePlayStream(
                                Role.HOST,
                                "steamdeck",
                                at("2026-11-03T21:55:11"),
                                at("2026-11-03T23:07:13"),
                                2909400L,
                                false),
                        new RemotePlayStream(
                                Role.HOST,
                                "steamdeck",
                                at("2026-11-04T19:38:38"),
                                at("2026-11-04T20:01:41"),
                                null,
                                null));
    }

    @Test
    public void hostStreamWithoutAStopEndsAtTheLastLineBeforeSteamRestarts() throws IOException {
        List<RemotePlayStream> streams =
                RemotePlayLogParser.hostStreams(fixture("streaming_log_remote_play_together.txt"), TORONTO);

        assertThat(streams)
                .containsExactly(
                        new RemotePlayStream(
                                Role.HOST,
                                "A Friend",
                                at("2024-11-09T13:15:07"),
                                at("2024-11-09T13:16:37"),
                                99300L,
                                true),
                        new RemotePlayStream(
                                Role.HOST,
                                "A Friend",
                                at("2024-11-09T13:23:38"),
                                at("2024-11-09T13:25:10"),
                                null,
                                null));
    }

    @Test
    public void hostStreamWithoutAStopEndsAtTheLastLineBeforeTheNextStream() {
        List<String> lines = List.of(
                "[2026-11-04 20:00:00][1.0] Streaming started to steamdeck at 192.168.50.55:1, audio channels = 2",
                "[2026-11-04 20:01:00][2.0] Encoding complete",
                "[2026-11-04 20:02:10][3.0] Streaming started to steamdeck at 192.168.50.55:2, audio channels = 2",
                "[2026-11-04 21:01:47][4.0] >>> Stopped desktop stream");

        assertThat(RemotePlayLogParser.hostStreams(lines, TORONTO))
                .extracting(RemotePlayStream::startedAt, RemotePlayStream::finishedAt)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(at("2026-11-04T20:00:00"), at("2026-11-04T20:01:00")),
                        org.assertj.core.groups.Tuple.tuple(at("2026-11-04T20:02:10"), at("2026-11-04T21:01:47")));
    }

    @Test
    public void hostStreamStillGoingIsLeftOut() {
        List<String> lines = List.of(
                "[2026-11-04 20:00:00][1.0] Streaming started to steamdeck at 192.168.50.55:1, audio channels = 2",
                "[2026-11-04 20:01:00][2.0] Encoding complete");

        assertThat(RemotePlayLogParser.hostStreams(lines, TORONTO)).isEmpty();
    }

    @Test
    public void desktopStreamStoppingOutsideAStreamIsIgnored() {
        List<String> lines = List.of("[2026-11-04 20:01:00][2.0] >>> Stopped desktop stream");

        assertThat(RemotePlayLogParser.hostStreams(lines, TORONTO)).isEmpty();
    }

    @Test
    public void clientStreamsPairStartsWithFinishes() throws IOException {
        List<RemotePlayStream> streams = RemotePlayLogParser.clientStreams(fixture("webhelper_js_client.txt"), TORONTO);

        assertThat(streams)
                .containsExactly(
                        new RemotePlayStream(
                                Role.CLIENT,
                                null,
                                at("2026-11-03T21:55:26"),
                                at("2026-11-03T23:07:13"),
                                2909400L,
                                null),
                        new RemotePlayStream(
                                Role.CLIENT,
                                null,
                                at("2026-11-04T19:38:38"),
                                at("2026-11-04T20:01:42"),
                                2909400L,
                                null));
    }

    @Test
    public void timestampsAreReadInTheGivenZone() {
        List<String> lines = List.of(
                "[2026-07-01 12:00:00] SteamUI: INFO: StreamingClientStarted for appid 1",
                "[2026-07-01 13:00:00] SteamUI: INFO: StreamingClientFinished for appid 1");

        assertThat(RemotePlayLogParser.clientStreams(lines, TORONTO).getFirst().startedAt())
                .isEqualTo(OffsetDateTime.parse("2026-07-01T12:00:00-04:00"));
    }

    private static OffsetDateTime at(String localDateTime) {
        return LocalDateTime.parse(localDateTime).atZone(TORONTO).toOffsetDateTime();
    }

    static List<String> fixture(String name) throws IOException {
        return Files.readAllLines(Path.of("src", "test", "resources", "remote-play", name));
    }
}
