package com.selesse.steam.registry;

import com.selesse.os.FilePathSanitizer;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.GameRegistries;
import com.selesse.steam.SteamApp;
import java.nio.file.Path;
import java.util.List;

public abstract class SteamRegistry {
    public static SteamRegistry getInstance() {
        return switch (OperatingSystems.get()) {
            case MAC -> new MacSteamRegistry();
            case WINDOWS -> new WindowsSteamRegistry();
            case LINUX, STEAM_OS -> new LinuxSteamRegistry();
        };
    }

    public abstract long getCurrentlyRunningAppId();

    public abstract List<Long> getInstalledAppIds();

    public Path getAppCachePath() {
        return Path.of(getBasePath(), "appcache/appinfo.vdf");
    }

    public Path getLibraryCachePath() {
        return Path.of(getBasePath(), "appcache/librarycache");
    }

    private String getBasePath() {
        return switch (OperatingSystems.get()) {
            case WINDOWS -> Path.of("C:\\Program Files (x86)\\Steam").toString();
            case MAC -> Path.of(FilePathSanitizer.sanitize("~/Library/Application Support/Steam"))
                    .toString();
            case LINUX -> Path.of(FilePathSanitizer.sanitize("~/.steam")).toString();
            case STEAM_OS -> Path.of(FilePathSanitizer.sanitize("~/.steam/steam")).toString();
        };
    }

    private SteamApp getSteamApp(long gameId) {
        return new SteamApp(GameRegistries.build().load(gameId));
    }
}
