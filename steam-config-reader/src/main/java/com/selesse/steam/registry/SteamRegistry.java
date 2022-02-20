package com.selesse.steam.registry;

import com.selesse.os.FilePathSanitizer;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.games.SteamGameMetadata;

import java.nio.file.Path;
import java.util.List;

public abstract class SteamRegistry {
    public static SteamRegistry getInstance() {
        return switch (OperatingSystems.get()) {
            case MAC -> new MacSteamRegistry();
            case WINDOWS -> new WindowsSteamRegistry();
            case LINUX -> new LinuxSteamRegistry();
        };
    }

    public abstract long getCurrentlyRunningAppId();
    public abstract List<Long> getInstalledAppIds();
    public abstract List<SteamGameMetadata> getGameMetadata();
    public abstract SteamGameMetadata getGameMetadata(Long gameId);

    public Path getAppCachePath() {
        return switch (OperatingSystems.get()) {
            case WINDOWS -> Path.of("C:\\Program Files (x86)\\Steam\\appcache\\appinfo.vdf");
            case MAC -> Path.of(FilePathSanitizer.sanitize("~/Library/Application Support/Steam/appcache/appinfo.vdf"));
            case LINUX -> Path.of(FilePathSanitizer.sanitize("~/.steam/appcache/appinfo.vdf"));
        };
    }
}
