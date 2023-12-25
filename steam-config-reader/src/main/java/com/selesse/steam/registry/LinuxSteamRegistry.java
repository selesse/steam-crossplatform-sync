package com.selesse.steam.registry;

import com.selesse.os.FilePathSanitizer;
import com.selesse.steam.processes.GameOverlayProcessLocator;
import java.nio.file.Path;

public class LinuxSteamRegistry extends FileBasedRegistry {
    @Override
    Path registryPath() {
        return Path.of(FilePathSanitizer.sanitize("~/.steam/registry.vdf"));
    }

    @Override
    public long getCurrentlyRunningAppId() {
        long currentlyRunningAppId = super.getCurrentlyRunningAppId();
        if (currentlyRunningAppId == 0) {
            return GameOverlayProcessLocator.locate()
                    .map(reaperProcess -> {
                        var processArguments = reaperProcess.info().arguments().orElse(new String[] {});
                        for (String argument : processArguments) {
                            if (argument.contains("AppId=")) {
                                return Long.parseLong(argument.split("=")[1]);
                            }
                        }
                        return 0L;
                    })
                    .orElse(0L);
        }
        return currentlyRunningAppId;
    }
}
