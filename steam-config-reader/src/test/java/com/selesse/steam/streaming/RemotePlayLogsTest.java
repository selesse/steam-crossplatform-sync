package com.selesse.steam.streaming;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.steam.streaming.RemotePlayStream.Role;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import org.junit.Test;

public class RemotePlayLogsTest {
    private static final ZoneId TORONTO = ZoneId.of("America/Toronto");

    @Test
    public void readsBothRolesFromTheLogsDirectory() throws IOException {
        Path logs = Files.createTempDirectory("steam-logs");
        Files.write(
                logs.resolve("streaming_log.txt"), RemotePlayLogParserTest.fixture("streaming_log_own_machines.txt"));
        Files.write(logs.resolve("webhelper_js.txt"), RemotePlayLogParserTest.fixture("webhelper_js_client.txt"));

        assertThat(RemotePlayLogs.read(logs, TORONTO))
                .extracting(RemotePlayStream::role)
                .containsExactly(Role.HOST, Role.HOST, Role.CLIENT, Role.CLIENT);
    }

    @Test
    public void streamStraddlingARotationIsReadWhole() throws IOException {
        Path logs = Files.createTempDirectory("steam-logs");
        Files.writeString(
                logs.resolve("streaming_log.previous.txt"),
                "[2026-11-04 20:00:00][1.0] Streaming started to steamdeck at 192.168.50.55:1, audio channels = 2\n");
        Files.writeString(logs.resolve("streaming_log.txt"), "[2026-11-04 21:00:00][2.0] >>> Stopped desktop stream\n");

        assertThat(RemotePlayLogs.read(logs, TORONTO)).hasSize(1);
    }

    @Test
    public void missingLogsMeanNoStreams() throws IOException {
        assertThat(RemotePlayLogs.read(Files.createTempDirectory("steam-logs"), TORONTO))
                .isEmpty();
    }

    @Test
    public void undecodableBytesDoNotStopTheRead() throws IOException {
        Path logs = Files.createTempDirectory("steam-logs");
        byte[] start = "[2026-11-04 20:00:00][1.0] Streaming started to café at 0.0.0.0:0, audio\n"
                .getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        byte[] stop = "[2026-11-04 21:00:00][2.0] >>> Stopped desktop stream\n".getBytes();
        Files.write(logs.resolve("streaming_log.txt"), concat(start, stop));

        assertThat(RemotePlayLogs.read(logs, TORONTO)).hasSize(1);
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] both = new byte[first.length + second.length];
        System.arraycopy(first, 0, both, 0, first.length);
        System.arraycopy(second, 0, both, first.length, second.length);
        return both;
    }
}
