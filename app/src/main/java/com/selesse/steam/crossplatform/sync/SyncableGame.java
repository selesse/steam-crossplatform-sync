package com.selesse.steam.crossplatform.sync;

import com.selesse.os.FilePathSanitizer;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steam.crossplatform.sync.serialize.SyncableGameRaw;

import java.nio.file.Path;

public record SyncableGame(String name, String windows, String mac, String linux, Long gameId) {
    public static SyncableGame fromRaw(SyncableGameRaw raw) {
        return new SyncableGame(raw.name(), raw.windows(), raw.mac(), raw.linux(), raw.gameId());
    }

    public Path getLocalPath() {
        return switch (OperatingSystems.get()) {
            case MAC -> Path.of(FilePathSanitizer.sanitize(mac));
            case LINUX -> Path.of(FilePathSanitizer.sanitize(linux));
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

    public Long getGameId() {
        return gameId;
    }

    public boolean isSupportedOnThisOs() {
        return switch (OperatingSystems.get()) {
            case MAC -> mac != null;
            case LINUX -> linux != null;
            case WINDOWS -> windows != null;
        };
    }
}
