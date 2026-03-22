package com.selesse.steam.crossplatform.sync.daemon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steamcrossplatformsync.gamesessions.GameSessionRecord;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Properties;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class HooksTest {
    private SteamCrossplatformSyncConfig config;
    private Path configDir;
    private Path hooksDir;

    @Before
    public void setup() throws IOException {
        configDir = Files.createTempDirectory("hooks-test");
        hooksDir = configDir.resolve("hooks");
        Files.createDirectories(hooksDir);

        config = mock(SteamCrossplatformSyncConfig.class);
        doReturn(configDir).when(config).getConfigDirectory();
    }

    @Test
    public void sessionEndHookReceivesSessionDataAsEnvironmentVariables() throws IOException {
        Path envOutput = configDir.resolve("env-output.properties");
        Path hook = writeExecutableScript(
                hooksDir.resolve("session-end"),
                "#!/bin/sh",
                "echo STEAM_APP_ID=$STEAM_APP_ID >> " + envOutput,
                "echo GAME_NAME=$GAME_NAME >> " + envOutput,
                "echo SESSION_STARTED_AT=$SESSION_STARTED_AT >> " + envOutput,
                "echo SESSION_ENDED_AT=$SESSION_ENDED_AT >> " + envOutput,
                "echo SESSION_DURATION_SECONDS=$SESSION_DURATION_SECONDS >> " + envOutput,
                "echo ACTIVE_PLAYTIME_SECONDS=$ACTIVE_PLAYTIME_SECONDS >> " + envOutput,
                "echo DB_PATH=$DB_PATH >> " + envOutput);

        var start = OffsetDateTime.parse("2026-01-01T12:00:00+00:00");
        var end = OffsetDateTime.parse("2026-01-01T13:00:00+00:00");
        var record = new GameSessionRecord(start, end, 1236720L, "Brotato", "my-host", 3000L);

        new SessionEndHook(record).run(config);

        assertThat(hook).exists();
        Properties props = new Properties();
        props.load(Files.newBufferedReader(envOutput));

        assertThat(props.getProperty("STEAM_APP_ID")).isEqualTo("1236720");
        assertThat(props.getProperty("GAME_NAME")).isEqualTo("Brotato");
        assertThat(props.getProperty("SESSION_STARTED_AT")).isEqualTo("2026-01-01T12:00Z");
        assertThat(props.getProperty("SESSION_ENDED_AT")).isEqualTo("2026-01-01T13:00Z");
        assertThat(props.getProperty("SESSION_DURATION_SECONDS")).isEqualTo("3600");
        assertThat(props.getProperty("ACTIVE_PLAYTIME_SECONDS")).isEqualTo("3000");
        assertThat(props.getProperty("DB_PATH")).isNotBlank();
    }

    @Test
    public void sessionEndHookReceivesEmptyGameNameWhenUnresolved() throws IOException {
        Path envOutput = configDir.resolve("env-output.properties");
        writeExecutableScript(
                hooksDir.resolve("session-end"), "#!/bin/sh", "echo GAME_NAME=$GAME_NAME >> " + envOutput);

        var record = new GameSessionRecord(OffsetDateTime.now(), OffsetDateTime.now(), 99L, null, "my-host", 0L);

        new SessionEndHook(record).run(config);

        Properties props = new Properties();
        props.load(Files.newBufferedReader(envOutput));
        assertThat(props.getProperty("GAME_NAME")).isEqualTo("");
    }

    @Test
    public void gameLoadErrorHookReceivesAppIdAndMessage() throws IOException {
        Path envOutput = configDir.resolve("env-output.properties");
        writeExecutableScript(
                hooksDir.resolve("game-load-error"),
                "#!/bin/sh",
                "echo STEAM_APP_ID=$STEAM_APP_ID >> " + envOutput,
                "echo ERROR_MESSAGE=$ERROR_MESSAGE >> " + envOutput);

        new GameLoadErrorHook(99L, new RuntimeException("steamcmd not found")).run(config);

        Properties props = new Properties();
        props.load(Files.newBufferedReader(envOutput));
        assertThat(props.getProperty("STEAM_APP_ID")).isEqualTo("99");
        assertThat(props.getProperty("ERROR_MESSAGE")).isEqualTo("steamcmd not found");
    }

    @Test
    public void batHookIsInvokedOnWindows() throws IOException {
        Assume.assumeTrue(
                "Skipping .bat hook test on non-Windows",
                OperatingSystems.get() == OperatingSystems.OperatingSystem.WINDOWS);
        Path envOutput = configDir.resolve("env-output.properties");
        Path hook = hooksDir.resolve("session-end.bat");
        Files.writeString(hook, "@echo off\r\necho DB_PATH=%DB_PATH%>> " + envOutput + "\r\n");

        var record = new GameSessionRecord(OffsetDateTime.now(), OffsetDateTime.now(), 1L, "Game", "host", 0L);

        new SessionEndHook(record).run(config);

        assertThat(hook).exists();
        Properties props = new Properties();
        props.load(Files.newBufferedReader(envOutput));
        assertThat(props.getProperty("DB_PATH")).isNotBlank();
    }

    @Test
    public void nonExecutableHookIsSkipped() throws IOException {
        Files.writeString(hooksDir.resolve("session-end"), "#!/bin/sh\nexit 0\n");

        var record = new GameSessionRecord(OffsetDateTime.now(), OffsetDateTime.now(), 1L, "Game", "host", 0L);

        new SessionEndHook(record).run(config);
    }

    @Test
    public void missingHookIsSkipped() {
        var record = new GameSessionRecord(OffsetDateTime.now(), OffsetDateTime.now(), 1L, "Game", "host", 0L);

        new SessionEndHook(record).run(config);
    }

    @Test
    public void failingHookDoesNotThrow() throws IOException {
        writeExecutableScript(hooksDir.resolve("session-end"), "#!/bin/sh", "exit 1");

        var record = new GameSessionRecord(OffsetDateTime.now(), OffsetDateTime.now(), 1L, "Game", "host", 0L);

        new SessionEndHook(record).run(config);
    }

    static Path writeExecutableScript(Path path, String... lines) throws IOException {
        Assume.assumeTrue(
                "Skipping script-based hook tests on Windows",
                OperatingSystems.get() != OperatingSystems.OperatingSystem.WINDOWS);
        Files.writeString(path, String.join("\n", lines) + "\n");
        Files.setPosixFilePermissions(
                path,
                EnumSet.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE,
                        PosixFilePermission.OWNER_EXECUTE));
        return path;
    }
}
