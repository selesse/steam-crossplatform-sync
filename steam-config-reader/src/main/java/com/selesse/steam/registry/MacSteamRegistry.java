package com.selesse.steam.registry;

import com.selesse.os.FilePathSanitizer;
import com.selesse.steam.processes.GameOverlayProcessLocator;
import java.nio.file.Path;
import java.util.Arrays;

class MacSteamRegistry extends FileBasedRegistry {
    @Override
    Path registryPath() {
        return Path.of(FilePathSanitizer.sanitize("~/Library/Application Support/Steam/registry.vdf"));
    }

    @Override
    public long getCurrentlyRunningAppId() {
        return GameOverlayProcessLocator.locate()
                .map(gameOverlayProcess -> {
                    var processArguments = gameOverlayProcess.info().arguments().orElse(new String[] {});
                    int gameIdIndex = Arrays.asList(processArguments).indexOf("-gameid");
                    if (gameIdIndex != -1 && processArguments.length >= gameIdIndex + 1) {
                        return Long.parseLong(processArguments[gameIdIndex + 1]);
                    }
                    return 0L;
                })
                .orElse(0L);
    }
}
