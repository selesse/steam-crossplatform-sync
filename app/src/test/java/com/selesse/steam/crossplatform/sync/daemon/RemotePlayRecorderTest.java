package com.selesse.steam.crossplatform.sync.daemon;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.steamcrossplatformsync.gamesessions.GameSessionRepository;
import com.selesse.steamcrossplatformsync.gamesessions.database.Database;
import com.selesse.steamcrossplatformsync.gamesessions.database.SqliteFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.ZoneId;
import org.junit.Before;
import org.junit.Test;

public class RemotePlayRecorderTest {
    private SqliteFile sqliteFile;
    private GameSessionRepository repository;
    private Path logs;

    @Before
    public void setup() throws IOException {
        Path testDatabase = Files.createTempDirectory("remote-play-recorder").resolve("test.sqlite3");
        sqliteFile = new SqliteFile(testDatabase);
        repository = new GameSessionRepository(sqliteFile);
        logs = Files.createTempDirectory("steam-logs");
    }

    @Test
    public void recordsEachStreamOnceAcrossReads() throws IOException, SQLException {
        Files.writeString(logs.resolve("streaming_log.txt"), """
                [2026-11-03 21:55:11][1.0] Streaming started to steamdeck at 192.168.50.55:50972, audio channels = 2
                [2026-11-03 23:07:13][2.0] >>> Stopped desktop stream
                """);
        var recorder = new RemotePlayRecorder(logs, () -> repository, "bench", ZoneId.of("America/Toronto"));

        recorder.run();
        recorder.run();

        try (var conn = Database.open(sqliteFile);
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery(
                        "SELECT host, role, remote_host, started_at, finished_at FROM remote_play_sessions")) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("host")).isEqualTo("bench");
            assertThat(rs.getString("role")).isEqualTo("host");
            assertThat(rs.getString("remote_host")).isEqualTo("steamdeck");
            assertThat(rs.getString("started_at")).isEqualTo("2026-11-03T21:55:11-05:00");
            assertThat(rs.getString("finished_at")).isEqualTo("2026-11-03T23:07:13-05:00");
            assertThat(rs.next()).isFalse();
        }
    }

    @Test
    public void noLogsRecordsNothing() {
        var recorder = new RemotePlayRecorder(
                logs,
                () -> {
                    throw new AssertionError("the database shouldn't be touched with nothing to record");
                },
                "bench",
                ZoneId.of("America/Toronto"));

        recorder.run();
    }
}
