package com.selesse.steam.registry;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.steamcmd.games.SteamGameMetadata;

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
            case MAC -> Path.of(System.getProperty("user.home") + "/Library/Application Support/Steam/appcache/appinfo.vdf");
            case LINUX -> Path.of(System.getProperty("user.home") + "/.config/steam/appcache/appinfo.vdf");
        };
    }
}
