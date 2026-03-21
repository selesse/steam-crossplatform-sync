package com.selesse.steam.crossplatform.sync.daemon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.selesse.steam.GameRunningDetector;
import com.selesse.steam.crossplatform.sync.SteamCrossplatformSyncContext;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steam.games.SteamGame;
import com.selesse.steam.processes.GameOverlayProcessLocator;
import com.selesse.steamcrossplatformsync.gamesessions.GameSessionRepository;
import com.selesse.steamcrossplatformsync.gamesessions.database.Database;
import com.selesse.steamcrossplatformsync.gamesessions.database.SqliteFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

public class GameMonitorTest {
    private GameSessionRepository repository;
    private SteamCrossplatformSyncContext context;
    private SteamGame brotato;
    private SteamGame hollowKnight;

    @Before
    public void setup() throws IOException {
        Path testDatabase =
                Path.of("src", "test", "resources", "game-monitor-test.sqlite3").toAbsolutePath();
        if (testDatabase.toFile().exists()) {
            assert (testDatabase.toFile().delete());
            testDatabase.toFile().deleteOnExit();
        }
        SqliteFile sqliteFile = new SqliteFile(testDatabase);
        Database.prepare(sqliteFile);
        repository = GameSessionRepository.getInstance(sqliteFile);

        // Stub sync config with an empty games file so sync is always a no-op in tests
        Path gamesFile = Files.createTempFile("games", ".yml");
        gamesFile.toFile().deleteOnExit();
        Files.writeString(gamesFile, "games: []\n");
        SteamCrossplatformSyncConfig syncConfig = mock(SteamCrossplatformSyncConfig.class);
        doReturn(gamesFile).when(syncConfig).getGamesFile();

        context = mock(SteamCrossplatformSyncContext.class);
        doReturn(syncConfig).when(context).getConfig();

        brotato = mock(SteamGame.class);
        doReturn(1236720L).when(brotato).getId();
        doReturn("Brotato").when(brotato).getName();

        hollowKnight = mock(SteamGame.class);
        doReturn(367520L).when(hollowKnight).getId();
        doReturn("Hollow Knight").when(hollowKnight).getName();
    }

    @Test
    public void knownGameLaunchAndClose_sessionSavedAndSyncTriggered() {
        try (MockedStatic<GameRunningDetector> detector = mockStatic(GameRunningDetector.class);
                MockedStatic<GameOverlayProcessLocator> locator = mockStatic(GameOverlayProcessLocator.class)) {
            locator.when(GameOverlayProcessLocator::locate).thenReturn(Optional.empty());
            doReturn(brotato).when(context).loadGame(1236720L);

            GameMonitor monitor = new GameMonitor(context);

            detector.when(GameRunningDetector::isGameCurrentlyRunning).thenReturn(true);
            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(1236720L);
            monitor.run();

            detector.when(GameRunningDetector::isGameCurrentlyRunning).thenReturn(false);
            monitor.run();
        }

        assertThat(repository.findUnknownGameIds()).isEmpty();
        verify(context, times(1)).getConfig();
    }

    @Test
    public void unknownGameLaunchAndClose_sessionSavedAndSyncNotTriggered() {
        try (MockedStatic<GameRunningDetector> detector = mockStatic(GameRunningDetector.class);
                MockedStatic<GameOverlayProcessLocator> locator = mockStatic(GameOverlayProcessLocator.class)) {
            locator.when(GameOverlayProcessLocator::locate).thenReturn(Optional.empty());
            doThrow(new RuntimeException("steam app not found")).when(context).loadGame(99L);

            GameMonitor monitor = new GameMonitor(context);

            detector.when(GameRunningDetector::isGameCurrentlyRunning).thenReturn(true);
            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(99L);
            monitor.run();

            detector.when(GameRunningDetector::isGameCurrentlyRunning).thenReturn(false);
            monitor.run();
        }

        assertThat(repository.findUnknownGameIds()).containsExactly(99L);
        verify(context, never()).getConfig();
    }

    @Test
    public void continuingGamePolls_syncOnlyTriggeredOnClose() {
        try (MockedStatic<GameRunningDetector> detector = mockStatic(GameRunningDetector.class);
                MockedStatic<GameOverlayProcessLocator> locator = mockStatic(GameOverlayProcessLocator.class)) {
            locator.when(GameOverlayProcessLocator::locate).thenReturn(Optional.empty());
            doReturn(brotato).when(context).loadGame(1236720L);

            GameMonitor monitor = new GameMonitor(context);

            detector.when(GameRunningDetector::isGameCurrentlyRunning).thenReturn(true);
            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(1236720L);
            monitor.run();
            monitor.run();
            monitor.run();

            verify(context, never()).getConfig();

            detector.when(GameRunningDetector::isGameCurrentlyRunning).thenReturn(false);
            monitor.run();

            verify(context, times(1)).getConfig();
        }
    }

    @Test
    public void gameSwitchKnownToKnown_bothSessionsSavedSyncTriggeredTwice() {
        try (MockedStatic<GameRunningDetector> detector = mockStatic(GameRunningDetector.class);
                MockedStatic<GameOverlayProcessLocator> locator = mockStatic(GameOverlayProcessLocator.class)) {
            locator.when(GameOverlayProcessLocator::locate).thenReturn(Optional.empty());
            doReturn(brotato).when(context).loadGame(1236720L);
            doReturn(hollowKnight).when(context).loadGame(367520L);

            GameMonitor monitor = new GameMonitor(context);

            detector.when(GameRunningDetector::isGameCurrentlyRunning).thenReturn(true);
            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(1236720L);
            monitor.run();

            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(367520L);
            monitor.run();

            detector.when(GameRunningDetector::isGameCurrentlyRunning).thenReturn(false);
            monitor.run();
        }

        assertThat(repository.findUnknownGameIds()).isEmpty();
        verify(context, times(2)).getConfig();
    }

    @Test
    public void gameSwitchUnknownToKnown_bothSessionsSavedSyncTriggeredOnce() {
        try (MockedStatic<GameRunningDetector> detector = mockStatic(GameRunningDetector.class);
                MockedStatic<GameOverlayProcessLocator> locator = mockStatic(GameOverlayProcessLocator.class)) {
            locator.when(GameOverlayProcessLocator::locate).thenReturn(Optional.empty());
            doThrow(new RuntimeException("steam app not found")).when(context).loadGame(99L);
            doReturn(brotato).when(context).loadGame(1236720L);

            GameMonitor monitor = new GameMonitor(context);

            detector.when(GameRunningDetector::isGameCurrentlyRunning).thenReturn(true);
            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(99L);
            monitor.run();

            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(1236720L);
            monitor.run();

            detector.when(GameRunningDetector::isGameCurrentlyRunning).thenReturn(false);
            monitor.run();
        }

        assertThat(repository.findUnknownGameIds()).containsExactly(99L);
        verify(context, times(1)).getConfig();
    }
}
