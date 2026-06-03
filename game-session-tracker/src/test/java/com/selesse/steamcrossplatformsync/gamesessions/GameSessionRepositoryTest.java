package com.selesse.steamcrossplatformsync.gamesessions;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.steamcrossplatformsync.gamesessions.database.Database;
import com.selesse.steamcrossplatformsync.gamesessions.database.SqliteFile;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
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
        Database.prepare(sqliteFile);
        this.repository = GameSessionRepository.getInstance(sqliteFile);
    }

    @Test
    public void databaseUsesWalJournalMode() throws SQLException {
        try (var conn = Database.getConnection(sqliteFile);
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
                3600);
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
                3600);
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
                3600));
        repository.save(new GameSessionRecord(
                OffsetDateTime.now(),
                OffsetDateTime.now().plus(1, ChronoUnit.HOURS),
                367520L,
                "Hollow Knight",
                "my-hostname",
                3600));

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
                3600));

        assertThat(repository.findUnknownGameIds()).containsExactly(1236720L);

        repository.updateGameName(1236720L, "Brotato");

        assertThat(repository.findUnknownGameIds()).isEmpty();
    }
}
