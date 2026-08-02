package com.selesse.steam.crossplatform.sync.daemon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
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
import java.util.OptionalLong;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.slf4j.LoggerFactory;

public class GameMonitorTest {
    private GameSessionRepository repository;
    private SteamCrossplatformSyncContext context;
    private SteamCrossplatformSyncConfig syncConfig;
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

        // Stub sync config with an empty games file so sync is always a no-op in tests,
        // and an empty config directory so HookRunner finds no hook to execute.
        Path gamesFile = Files.createTempFile("games", ".yml");
        gamesFile.toFile().deleteOnExit();
        Files.writeString(gamesFile, "games: []\n");
        Path emptyConfigDir = Files.createTempDirectory("game-monitor-test-config");
        syncConfig = mock(SteamCrossplatformSyncConfig.class);
        doReturn(gamesFile).when(syncConfig).getGamesFile();
        doReturn(emptyConfigDir).when(syncConfig).getConfigDirectory();

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

            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(OptionalLong.of(1236720L));
            monitor.run();

            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(OptionalLong.empty());
            monitor.run();
        }

        assertThat(repository.findUnknownGameIds()).isEmpty();
        verify(syncConfig, times(1)).getGamesFile();
    }

    @Test
    public void unknownGameLaunchAndClose_sessionSavedAndSyncNotTriggered() {
        try (MockedStatic<GameRunningDetector> detector = mockStatic(GameRunningDetector.class);
                MockedStatic<GameOverlayProcessLocator> locator = mockStatic(GameOverlayProcessLocator.class)) {
            locator.when(GameOverlayProcessLocator::locate).thenReturn(Optional.empty());
            doThrow(new RuntimeException("steam app not found")).when(context).loadGame(99L);

            GameMonitor monitor = new GameMonitor(context);

            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(OptionalLong.of(99L));
            monitor.run();

            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(OptionalLong.empty());
            monitor.run();
        }

        assertThat(repository.findUnknownGameIds()).containsExactly(99L);
        verify(syncConfig, never()).getGamesFile();
    }

    @Test
    public void continuingGamePolls_syncOnlyTriggeredOnClose() {
        try (MockedStatic<GameRunningDetector> detector = mockStatic(GameRunningDetector.class);
                MockedStatic<GameOverlayProcessLocator> locator = mockStatic(GameOverlayProcessLocator.class)) {
            locator.when(GameOverlayProcessLocator::locate).thenReturn(Optional.empty());
            doReturn(brotato).when(context).loadGame(1236720L);

            GameMonitor monitor = new GameMonitor(context);

            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(OptionalLong.of(1236720L));
            monitor.run();
            monitor.run();
            monitor.run();

            verify(syncConfig, never()).getGamesFile();

            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(OptionalLong.empty());
            monitor.run();

            verify(syncConfig, times(1)).getGamesFile();
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

            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(OptionalLong.of(1236720L));
            monitor.run();

            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(OptionalLong.of(367520L));
            monitor.run();

            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(OptionalLong.empty());
            monitor.run();
        }

        assertThat(repository.findUnknownGameIds()).isEmpty();
        verify(syncConfig, times(2)).getGamesFile();
    }

    @Test
    public void gameSwitchUnknownToKnown_bothSessionsSavedSyncTriggeredOnce() {
        try (MockedStatic<GameRunningDetector> detector = mockStatic(GameRunningDetector.class);
                MockedStatic<GameOverlayProcessLocator> locator = mockStatic(GameOverlayProcessLocator.class)) {
            locator.when(GameOverlayProcessLocator::locate).thenReturn(Optional.empty());
            doThrow(new RuntimeException("steam app not found")).when(context).loadGame(99L);
            doReturn(brotato).when(context).loadGame(1236720L);

            GameMonitor monitor = new GameMonitor(context);

            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(OptionalLong.of(99L));
            monitor.run();

            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(OptionalLong.of(1236720L));
            monitor.run();

            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(OptionalLong.empty());
            monitor.run();
        }

        assertThat(repository.findUnknownGameIds()).containsExactly(99L);
        verify(syncConfig, times(1)).getGamesFile();
    }

    @Test
    public void unknownGameLaunch_firesGameLoadErrorHook() throws IOException, InterruptedException {
        Path hookConfigDir = Files.createTempDirectory("game-monitor-hook-test-config");
        Path hookOutput = hookConfigDir.resolve("hook-output.properties");
        HooksTest.writeExecutableScript(
                Files.createDirectories(hookConfigDir.resolve("hooks")).resolve("game-load-error"),
                "#!/bin/sh",
                "echo STEAM_APP_ID=$STEAM_APP_ID >> " + hookOutput,
                "echo ERROR_MESSAGE=$ERROR_MESSAGE >> " + hookOutput);
        doReturn(hookConfigDir).when(syncConfig).getConfigDirectory();

        try (MockedStatic<GameRunningDetector> detector = mockStatic(GameRunningDetector.class);
                MockedStatic<GameOverlayProcessLocator> locator = mockStatic(GameOverlayProcessLocator.class)) {
            locator.when(GameOverlayProcessLocator::locate).thenReturn(Optional.empty());
            doThrow(new RuntimeException("steamcmd not found")).when(context).loadGame(99L);

            GameMonitor monitor = new GameMonitor(context);

            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(OptionalLong.of(99L));
            monitor.run();
        }

        long deadline = System.currentTimeMillis() + 5_000;
        while (!Files.exists(hookOutput) && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }

        Properties props = new Properties();
        props.load(Files.newBufferedReader(hookOutput));
        assertThat(props.getProperty("STEAM_APP_ID")).isEqualTo("99");
        assertThat(props.getProperty("ERROR_MESSAGE")).isEqualTo("steamcmd not found");
    }

    // Regression test: KnownGame.onClosed() used to run sync and the session-end hook
    // sequentially in a way where a sync failure meant the hook line was never reached at all -
    // no notification, nothing. They're independent concerns; a sync failure shouldn't prevent
    // the hook from running.
    @Test
    public void sessionEndHookStillRunsWhenSyncFails() throws IOException, InterruptedException {
        Path hookConfigDir = Files.createTempDirectory("game-monitor-hook-test-config");
        Path hookOutput = hookConfigDir.resolve("hook-output.properties");
        HooksTest.writeExecutableScript(
                Files.createDirectories(hookConfigDir.resolve("hooks")).resolve("session-end"),
                "#!/bin/sh",
                "echo STEAM_APP_ID=$STEAM_APP_ID >> " + hookOutput);
        doReturn(hookConfigDir).when(syncConfig).getConfigDirectory();
        doThrow(new RuntimeException("boom")).when(syncConfig).getGamesFile();

        try (MockedStatic<GameRunningDetector> detector = mockStatic(GameRunningDetector.class);
                MockedStatic<GameOverlayProcessLocator> locator = mockStatic(GameOverlayProcessLocator.class)) {
            locator.when(GameOverlayProcessLocator::locate).thenReturn(Optional.empty());
            doReturn(brotato).when(context).loadGame(1236720L);

            GameMonitor monitor = new GameMonitor(context);
            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(OptionalLong.of(1236720L));
            monitor.run();

            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenReturn(OptionalLong.empty());
            monitor.run();
        }

        long deadline = System.currentTimeMillis() + 5_000;
        while (!Files.exists(hookOutput) && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }

        Properties props = new Properties();
        props.load(Files.newBufferedReader(hookOutput));
        assertThat(props.getProperty("STEAM_APP_ID")).isEqualTo("1236720");
    }

    // Regression test: run() is invoked both from a scheduled poll and, via
    // startTracking()'s processHandle.onExit().thenRunAsync(this), from a callback with no caller
    // able to observe a thrown exception. Anything that escapes run() on that path used to vanish
    // silently - nothing indicated a failure had occurred at all. run() now catches and logs
    // internally, so this is fully synchronous: no threading or async waiting needed to prove the
    // fix. (KnownGame.onClosed() now catches sync failures itself with a more specific message -
    // see sessionEndHookStillRunsWhenSyncFails - so this uses a failure from GameRunningDetector
    // to exercise run()'s own catch instead.)
    @Test
    public void runNeverThrowsAndLogsOnUnexpectedFailure() {
        var gameMonitorLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(GameMonitor.class);
        ListAppender<ILoggingEvent> logCapture = new ListAppender<>();
        logCapture.start();
        gameMonitorLogger.addAppender(logCapture);

        try (MockedStatic<GameRunningDetector> detector = mockStatic(GameRunningDetector.class)) {
            detector.when(GameRunningDetector::getCurrentlyRunningGameId).thenThrow(new RuntimeException("boom"));

            GameMonitor monitor = new GameMonitor(context);
            assertThatCode(monitor::run).doesNotThrowAnyException();
        } finally {
            gameMonitorLogger.detachAppender(logCapture);
        }

        assertThat(logCapture.list)
                .anySatisfy(event -> assertThat(event.getFormattedMessage()).isEqualTo("Game monitor run failed"));
    }
}
