package com.selesse.steam.crossplatform.sync;

import com.selesse.os.FilePathSanitizer;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steam.crossplatform.sync.serialize.SyncableGameRaw;

import java.nio.file.Path;

public class SyncableGame {
    private final String name;
    private final String windows;
    private final String mac;
    private final Long gameId;

    private SyncableGame(String name, String windows, String mac, long gameId) {
        this.name = name;
        this.windows = windows;
        this.mac = mac;
        this.gameId = gameId;
    }

    public static SyncableGame fromRaw(SyncableGameRaw raw) {
        return new SyncableGame(raw.name, raw.windows, raw.mac, raw.gameId);
    }

    public Path getLocalPath() {
        return switch (OperatingSystems.get()) {
            case MAC -> Path.of(FilePathSanitizer.sanitize(mac));
            case WINDOWS -> Path.of(FilePathSanitizer.sanitize(windows));
        };
    }

    public Path getLocalCloudSyncPath(SteamCrossplatformSyncConfig config) {
        return Path.of(
                config.getLocalCloudSyncBaseDirectory().toAbsolutePath().toString(),
                "games",
                // This is definitely gonna be a problem at some point...
                name.toLowerCase().replaceAll(" ", "_")
        );
    }

    public String getName() {
        return name;
    }
}
