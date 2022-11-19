package com.selesse.steamcrossplatformsync.gamesessions;

import static org.mockito.Mockito.doReturn;

import com.selesse.steam.games.SteamGame;
import com.selesse.steamcrossplatformsync.gamesessions.database.SqliteFile;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class GameSessionRepositoryTest {
    private SqliteFile testDatabase;

    @Before
    public void setup() {
        Path testDatabase = Path.of("src", "test", "resources", "test.sqlite3").toAbsolutePath();
        if (testDatabase.toFile().exists()) {
            assert (testDatabase.toFile().delete());
            testDatabase.toFile().deleteOnExit();
        }
        this.testDatabase = new SqliteFile(testDatabase);
    }

    @Test
    public void savePersistsToTheDatabase() {
        SteamGame mockGame = Mockito.mock(SteamGame.class);
        doReturn("Hollow Knight").when(mockGame).getName();
        doReturn(367520L).when(mockGame).getId();
        GameSessionRecord gameSessionRecord = new GameSessionRecord(
                OffsetDateTime.now(), OffsetDateTime.now().plus(1, ChronoUnit.HOURS), mockGame, "my-hostname");
        GameSessionRepository.getInstance(testDatabase).save(gameSessionRecord);
    }
}
