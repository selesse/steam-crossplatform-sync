package com.selesse.steam.registry;

import com.selesse.os.FilePathSanitizer;
import com.selesse.os.OperatingSystems;
import java.nio.file.Path;

public class SteamRegistry {
    public static SteamRegistry getInstance() {
        return new SteamRegistry();
    }

    public Path getAppCachePath() {
        return Path.of(getBasePath(), "appcache/appinfo.vdf");
    }

    public Path getSteamAppsPath() {
        return Path.of(getBasePath(), "steamapps");
    }

    private String getBasePath() {
        return switch (OperatingSystems.get()) {
            case WINDOWS -> Path.of("C:\\Program Files (x86)\\Steam").toString();
            case MAC ->
                Path.of(FilePathSanitizer.sanitize("~/Library/Application Support/Steam"))
                        .toString();
            case LINUX -> Path.of(FilePathSanitizer.sanitize("~/.steam")).toString();
            case STEAM_OS ->
                Path.of(FilePathSanitizer.sanitize("~/.steam/steam")).toString();
        };
    }
}
