package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steamcrossplatformsync.gamesessions.GameSessionRecord;
import com.selesse.steamcrossplatformsync.gamesessions.database.SqliteDatabaseLocation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HookRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(HookRunner.class);

    static void runSessionEndHook(SteamCrossplatformSyncConfig config, GameSessionRecord record) {
        Path hookPath = config.getConfigDirectory().resolve("hooks").resolve("session-end");
        if (!Files.isRegularFile(hookPath) || !Files.isExecutable(hookPath)) {
            return;
        }
        try {
            ProcessBuilder pb = new ProcessBuilder(hookPath.toAbsolutePath().toString());
            pb.environment().put("STEAM_APP_ID", String.valueOf(record.gameId()));
            pb.environment().put("GAME_NAME", record.gameName() != null ? record.gameName() : "");
            pb.environment().put("SESSION_STARTED_AT", record.startedAt().toString());
            pb.environment().put("SESSION_ENDED_AT", record.finishedAt().toString());
            long durationSeconds =
                    Duration.between(record.startedAt(), record.finishedAt()).getSeconds();
            pb.environment().put("SESSION_DURATION_SECONDS", String.valueOf(durationSeconds));
            pb.environment().put("ACTIVE_PLAYTIME_SECONDS", String.valueOf(record.activePlaytimeSeconds()));
            pb.environment()
                    .put(
                            "DB_PATH",
                            SqliteDatabaseLocation.get().path().toAbsolutePath().toString());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            LOGGER.info("Hook session-end exited {}{}", exitCode, output.isBlank() ? "" : " " + output.strip());
        } catch (Exception e) {
            LOGGER.warn("Hook session-end failed", e);
        }
    }
}
