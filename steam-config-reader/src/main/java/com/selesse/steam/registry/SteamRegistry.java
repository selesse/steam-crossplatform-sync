package com.selesse.steam.registry;

import com.selesse.os.FilePathSanitizer;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.AppType;
import com.selesse.steam.GameRegistries;
import com.selesse.steam.SteamApp;
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

    public List<SteamGameMetadata> getGamesMetadata() {
        return getInstalledAppIds().stream()
                .map(this::getGameMetadata)
                .filter(gameMetadata -> getSteamApp(gameMetadata.gameId()).getType() == AppType.GAME)
                .toList();
    }

    public SteamGameMetadata getGameMetadata(Long gameId) {
        SteamApp steamApp = getSteamApp(gameId);
        return new SteamGameMetadata(
                gameId, steamApp.getName(), getInstalledAppIds().contains(gameId));
    }

    public Path getAppCachePath() {
        return switch (OperatingSystems.get()) {
            case WINDOWS -> Path.of("C:\\Program Files (x86)\\Steam\\appcache\\appinfo.vdf");
            case MAC -> Path.of(FilePathSanitizer.sanitize("~/Library/Application Support/Steam/appcache/appinfo.vdf"));
            case LINUX -> Path.of(FilePathSanitizer.sanitize("~/.steam/appcache/appinfo.vdf"));
        };
    }

    private SteamApp getSteamApp(long gameId) {
        return new SteamApp(GameRegistries.build().load(gameId));
    }
}
