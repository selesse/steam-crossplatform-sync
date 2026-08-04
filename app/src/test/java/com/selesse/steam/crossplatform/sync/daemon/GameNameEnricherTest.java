package com.selesse.steam.crossplatform.sync.daemon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.selesse.steam.SteamApp;
import com.selesse.steam.crossplatform.sync.SteamCrossplatformSyncContext;
import com.selesse.steamcrossplatformsync.gamesessions.GameSessionRecord;
import com.selesse.steamcrossplatformsync.gamesessions.GameSessionRepository;
import com.selesse.steamcrossplatformsync.gamesessions.database.SqliteFile;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.Before;
import org.junit.Test;

public class GameNameEnricherTest {
    private GameSessionRepository repository;
    private SteamCrossplatformSyncContext context;

    @Before
    public void setup() {
        Path testDatabase =
                Path.of("src", "test", "resources", "enricher-test.sqlite3").toAbsolutePath();
        if (testDatabase.toFile().exists()) {
            assert (testDatabase.toFile().delete());
            testDatabase.toFile().deleteOnExit();
        }
        SqliteFile sqliteFile = new SqliteFile(testDatabase);
        repository = GameSessionRepository.getInstance(sqliteFile);
        context = mock(SteamCrossplatformSyncContext.class);
    }

    @Test
    public void enricherBackfillsNameWhenGameCanBeResolved() {
        repository.save(new GameSessionRecord(
                OffsetDateTime.now(),
                OffsetDateTime.now().plus(1, ChronoUnit.HOURS),
                1236720L,
                null,
                "my-hostname",
                3600));

        SteamApp mockGame = mock(SteamApp.class);
        doReturn("Brotato").when(mockGame).getName();
        doReturn(mockGame).when(context).loadGame(1236720L);

        new GameNameEnricher(context).run();

        assertThat(repository.findUnknownGameIds()).isEmpty();
    }

    @Test
    public void enricherLeavesNameNullWhenGameCannotBeResolved() {
        repository.save(new GameSessionRecord(
                OffsetDateTime.now(),
                OffsetDateTime.now().plus(1, ChronoUnit.HOURS),
                1236720L,
                null,
                "my-hostname",
                3600));

        doThrow(new RuntimeException("steamcmd not found")).when(context).loadGame(1236720L);

        new GameNameEnricher(context).run();

        assertThat(repository.findUnknownGameIds()).containsExactly(1236720L);
    }
}
