package com.selesse.steam.crossplatform.sync;

import com.selesse.files.PatternSupportedPath;
import com.selesse.os.FilePathSanitizer;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steam.crossplatform.sync.serialize.SyncableGameRaw;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public record SyncableGame(
        String name, List<String> windows, List<String> mac, List<String> linux, Long gameId, boolean sync) {
    public static SyncableGame fromRaw(SyncableGameRaw raw) {
        return new SyncableGame(raw.name(), raw.windows(), raw.mac(), raw.linux(), raw.gameId(), raw.sync());
    }

    public List<PatternSupportedPath> getLocalPaths() {
        return switch (OperatingSystems.get()) {
            case WINDOWS -> windows().stream()
                    .map(path -> PatternSupportedPath.of(FilePathSanitizer.sanitize(path)))
                    .toList();
            case MAC -> mac().stream()
                    .map(path -> PatternSupportedPath.of(FilePathSanitizer.sanitize(path)))
                    .toList();
            case LINUX -> linux().stream()
                    .map(path -> PatternSupportedPath.of(FilePathSanitizer.sanitize(path)))
                    .toList();
        };
    }

    public Path getLocalCloudSyncPath(SteamCrossplatformSyncConfig config) {
        return Path.of(
                config.getLocalCloudSyncBaseDirectory().toAbsolutePath().toString(),
                "games",
                // This is definitely gonna be a problem at some point...
                name.toLowerCase().replaceAll(" ", "_"));
    }

    public String getName() {
        return name;
    }

    public Long getGameId() {
        return gameId;
    }

    public boolean isSupportedOnThisOs() {
        return switch (OperatingSystems.get()) {
            case WINDOWS -> Optional.ofNullable(windows())
                    .map(x -> !x.isEmpty())
                    .orElse(false);
            case MAC -> Optional.ofNullable(mac()).map(x -> !x.isEmpty()).orElse(false);
            case LINUX -> Optional.ofNullable(linux()).map(x -> !x.isEmpty()).orElse(false);
        };
    }
}
