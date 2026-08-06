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

// Without @JsonIncludeProperties, Jackson treats the derived accessors below as properties too,
// and games.yml gains a localPaths and a supportedOnThisOs key describing whichever machine
// happened to generate it.
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({"name", "gameId", "windows", "mac", "linux", "sync"})
@JsonPropertyOrder({"name", "gameId", "windows", "mac", "linux", "sync"})
public record SyncableGame(
        String name, List<String> windows, List<String> mac, List<String> linux, long gameId, boolean sync) {

    public List<PatternSupportedPath> getLocalPaths() {
        return pathsForThisOs().stream()
                .map(path -> PatternSupportedPath.of(FilePathSanitizer.sanitize(path)))
                .toList();
    }

    /** The configured paths for the OS we're running on, empty when the game doesn't run here. */
    private List<String> pathsForThisOs() {
        List<String> paths =
                switch (OperatingSystems.get().family()) {
                    case WINDOWS -> windows();
                    case MAC -> mac();
                    case LINUX -> linux();
                };
        // A game that doesn't run on this OS simply omits the key.
        return paths == null ? List.of() : paths;
    }

    public Path getLocalCloudSyncPath(SteamCrossplatformSyncConfig config) {
        return Path.of(
                config.getLocalCloudSyncBaseDirectory().toAbsolutePath().toString(),
                "games",
                name.toLowerCase().replaceAll(" ", "_").replaceAll("[^a-z0-9_]", ""));
    }

    public boolean isSupportedOnThisOs() {
        return !pathsForThisOs().isEmpty();
    }
}
