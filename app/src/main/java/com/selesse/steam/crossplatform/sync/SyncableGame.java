package com.selesse.steam.crossplatform.sync;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.selesse.files.PatternSupportedPath;
import com.selesse.os.FilePathSanitizer;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

// Without @JsonIncludeProperties, Jackson also writes out the derived accessors below, and
// getLocalPaths() throws on the way.
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({"name", "gameId", "windows", "mac", "linux", "sync"})
@JsonPropertyOrder({"name", "gameId", "windows", "mac", "linux", "sync"})
public record SyncableGame(
        String name, List<String> windows, List<String> mac, List<String> linux, long gameId, boolean sync) {

    public List<PatternSupportedPath> getLocalPaths() {
        return switch (OperatingSystems.get().family()) {
            case WINDOWS ->
                windows().stream()
                        .map(path -> PatternSupportedPath.of(FilePathSanitizer.sanitize(path)))
                        .toList();
            case MAC ->
                mac().stream()
                        .map(path -> PatternSupportedPath.of(FilePathSanitizer.sanitize(path)))
                        .toList();
            case LINUX ->
                linux().stream()
                        .map(path -> PatternSupportedPath.of(FilePathSanitizer.sanitize(path)))
                        .toList();
        };
    }

    public Path getLocalCloudSyncPath(SteamCrossplatformSyncConfig config) {
        return Path.of(
                config.getLocalCloudSyncBaseDirectory().toAbsolutePath().toString(),
                "games",
                name.toLowerCase().replaceAll(" ", "_").replaceAll("[^a-z0-9_]", ""));
    }

    public boolean isSupportedOnThisOs() {
        return switch (OperatingSystems.get().family()) {
            case WINDOWS ->
                Optional.ofNullable(windows()).map(x -> !x.isEmpty()).orElse(false);
            case MAC -> Optional.ofNullable(mac()).map(x -> !x.isEmpty()).orElse(false);
            case LINUX -> Optional.ofNullable(linux()).map(x -> !x.isEmpty()).orElse(false);
        };
    }
}
