package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.os.Hostnames;
import com.selesse.steam.crossplatform.sync.SteamCrossplatformSyncContext;
import com.selesse.steam.streaming.RemotePlayLogs;
import com.selesse.steam.streaming.RemotePlayStream;
import com.selesse.steamcrossplatformsync.gamesessions.GameSessionRepository;
import com.selesse.steamcrossplatformsync.gamesessions.RemotePlaySessionRecord;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Records the Remote Play streams in Steam's logs. Recording is idempotent, so this reads the logs
 * whole every time instead of tracking how far it got.
 */
class RemotePlayRecorder implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemotePlayRecorder.class);

    private final Path logsDirectory;
    private final Supplier<GameSessionRepository> repository;
    private final String host;
    private final ZoneId zone;

    RemotePlayRecorder(Path logsDirectory, Supplier<GameSessionRepository> repository, String host, ZoneId zone) {
        this.logsDirectory = logsDirectory;
        this.repository = repository;
        this.host = host;
        this.zone = zone;
    }

    static RemotePlayRecorder forContext(SteamCrossplatformSyncContext context) {
        return new RemotePlayRecorder(
                context.getSteamInstall().registry().getLogsPath(),
                context::getSessionRepository,
                Hostnames.getCurrent(),
                ZoneId.systemDefault());
    }

    @Override
    public synchronized void run() {
        List<RemotePlaySessionRecord> records = RemotePlayLogs.read(logsDirectory, zone).stream()
                .map(this::toRecord)
                .toList();
        if (records.isEmpty()) {
            return;
        }
        int recorded = repository.get().saveRemotePlaySessions(records);
        if (recorded > 0) {
            LOGGER.info("Recorded {} Remote Play stream(s)", recorded);
        }
    }

    private RemotePlaySessionRecord toRecord(RemotePlayStream stream) {
        RemotePlaySessionRecord.Role role =
                switch (stream.role()) {
                    case HOST -> RemotePlaySessionRecord.Role.HOST;
                    case CLIENT -> RemotePlaySessionRecord.Role.CLIENT;
                };
        return new RemotePlaySessionRecord(
                host,
                role,
                stream.remoteHost(),
                stream.startedAt(),
                stream.finishedAt(),
                stream.appId(),
                stream.remotePlayTogether());
    }
}
