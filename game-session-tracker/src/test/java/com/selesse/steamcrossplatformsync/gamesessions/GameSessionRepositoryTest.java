package com.selesse.steamcrossplatformsync.gamesessions;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.steamcrossplatformsync.gamesessions.database.Database;
import com.selesse.steamcrossplatformsync.gamesessions.database.SqliteFile;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class GameSessionRepositoryTest {
    private GameSessionRepository repository;
    private SqliteFile sqliteFile;

    @Before
    public void setup() {
        Path testDatabase = Path.of("src", "test", "resources", "test.sqlite3").toAbsolutePath();
        if (testDatabase.toFile().exists()) {
            assert (testDatabase.toFile().delete());
            testDatabase.toFile().deleteOnExit();
        }
        this.sqliteFile = new SqliteFile(testDatabase);
        this.repository = new GameSessionRepository(sqliteFile);
    }

    @Test
    public void databaseUsesWalJournalMode() throws SQLException {
        try (var conn = Database.open(sqliteFile);
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery("PRAGMA journal_mode")) {
            assertThat(rs.getString(1)).isEqualTo("wal");
        }
    }

    @Test
    public void savePersistsToTheDatabase() {
        GameSessionRecord record = new GameSessionRecord(
                OffsetDateTime.now(),
                OffsetDateTime.now().plus(1, ChronoUnit.HOURS),
                367520L,
                "Hollow Knight",
                "my-hostname",
                3600,
                null);
        repository.save(record);
    }

    @Test
    public void saveWithNullNamePersistsToTheDatabase() {
        GameSessionRecord record = new GameSessionRecord(
                OffsetDateTime.now(),
                OffsetDateTime.now().plus(1, ChronoUnit.HOURS),
                1236720L,
                null,
                "my-hostname",
                3600,
                null);
        repository.save(record);
    }

    @Test
    public void findUnknownGameIdsReturnsIdsWithNullNames() {
        repository.save(new GameSessionRecord(
                OffsetDateTime.now(),
                OffsetDateTime.now().plus(1, ChronoUnit.HOURS),
                1236720L,
                null,
                "my-hostname",
                3600,
                null));
        repository.save(new GameSessionRecord(
                OffsetDateTime.now(),
                OffsetDateTime.now().plus(1, ChronoUnit.HOURS),
                367520L,
                "Hollow Knight",
                "my-hostname",
                3600,
                null));

        assertThat(repository.findUnknownGameIds()).containsExactly(1236720L);
    }

    @Test
    public void updateGameNameSetsTheNameForTheGivenAppId() {
        repository.save(new GameSessionRecord(
                OffsetDateTime.now(),
                OffsetDateTime.now().plus(1, ChronoUnit.HOURS),
                1236720L,
                null,
                "my-hostname",
                3600,
                null));

        assertThat(repository.findUnknownGameIds()).containsExactly(1236720L);

        repository.updateGameName(1236720L, "Brotato");

        assertThat(repository.findUnknownGameIds()).isEmpty();
    }

    @Test
    public void savePersistsTheSteamAccountId() throws SQLException {
        repository.save(new GameSessionRecord(
                OffsetDateTime.now(),
                OffsetDateTime.now().plus(1, ChronoUnit.HOURS),
                367520L,
                "Hollow Knight",
                "my-hostname",
                3600,
                76561197960287930L));

        assertThat(savedSteamAccountIds()).containsExactly(76561197960287930L);
    }

    @Test
    public void savePersistsAnUnknownSteamAccountIdAsNull() throws SQLException {
        repository.save(new GameSessionRecord(
                OffsetDateTime.now(),
                OffsetDateTime.now().plus(1, ChronoUnit.HOURS),
                367520L,
                "Hollow Knight",
                "my-hostname",
                3600,
                null));

        assertThat(savedSteamAccountIds()).containsExactly((Long) null);
    }

    @Test
    public void saveRemotePlaySessionsSkipsStreamsAlreadyRecorded() throws SQLException {
        OffsetDateTime start = OffsetDateTime.parse("2026-11-03T21:55:11-05:00");
        var stream = new RemotePlaySessionRecord(
                "bench", RemotePlaySessionRecord.Role.HOST, "steamdeck", start, start.plusMinutes(72), 2909400L, false);
        var clientStream = new RemotePlaySessionRecord(
                "steamdeck", RemotePlaySessionRecord.Role.CLIENT, null, start, start.plusMinutes(72), null, null);

        assertThat(repository.saveRemotePlaySessions(List.of(stream, clientStream)))
                .isEqualTo(2);
        assertThat(repository.saveRemotePlaySessions(List.of(stream, clientStream)))
                .isZero();

        try (var conn = Database.open(sqliteFile);
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery("SELECT host, role, remote_host, started_at, steam_app_id,"
                        + " remote_play_together FROM remote_play_sessions ORDER BY role DESC")) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("host")).isEqualTo("bench");
            assertThat(rs.getString("role")).isEqualTo("host");
            assertThat(rs.getString("remote_host")).isEqualTo("steamdeck");
            assertThat(rs.getString("started_at")).isEqualTo("2026-11-03T21:55:11-05:00");
            assertThat(rs.getLong("steam_app_id")).isEqualTo(2909400L);
            assertThat(rs.getInt("remote_play_together")).isZero();
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("role")).isEqualTo("client");
            assertThat(rs.getObject("remote_host")).isNull();
            assertThat(rs.getObject("steam_app_id")).isNull();
            assertThat(rs.next()).isFalse();
        }
    }

    private List<Long> savedSteamAccountIds() throws SQLException {
        try (var conn = Database.open(sqliteFile);
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery("SELECT steam_account_id FROM gaming_sessions")) {
            List<Long> ids = new ArrayList<>();
            while (rs.next()) {
                long id = rs.getLong(1);
                ids.add(rs.wasNull() ? null : id);
            }
            return ids;
        }
    }
}
