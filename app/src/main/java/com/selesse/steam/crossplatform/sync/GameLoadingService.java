package com.selesse.steam.crossplatform.sync;

import com.selesse.files.RuntimeExceptionFiles;
import com.selesse.steam.GameRegistries;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steam.games.SteamGame;
import com.selesse.steam.games.SteamGameMetadata;
import com.selesse.steam.registry.RegistryPrettyPrint;
import com.selesse.steam.registry.SteamRegistry;
import com.selesse.steam.registry.implementation.RegistryParser;
import com.selesse.steam.registry.implementation.RegistryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class GameLoadingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameLoadingService.class);
    private final GameRegistries gameRegistries;
    private final SteamRegistry steamRegistry;
    private final SteamCrossplatformSyncConfig config;

    public GameLoadingService(SteamCrossplatformSyncConfig config) {
        this.config = config;
        if (config.getRemoteAppInfoUrl() != null) {
            gameRegistries = GameRegistries.buildWithRemoteFallback(config.getRemoteAppInfoUrl());
        } else {
            gameRegistries = GameRegistries.build();
        }
        steamRegistry = SteamRegistry.getInstance();
    }

    public SteamGame loadGame(long gameId) {
        RegistryStore registryStore = null;

        Path cachedRegistryStore = Path.of(config.getCacheDirectory().toString(), gameId + ".vdf");
        if (accurateEnoughCache(cachedRegistryStore)) {
            List<String> lines = RuntimeExceptionFiles.readAllLines(cachedRegistryStore);
            if (lines.size() > 3) {
                registryStore = RegistryParser.parse(lines);
            }
        }
        if (registryStore == null) {
            registryStore = gameRegistries.load(gameId);
            RuntimeExceptionFiles.writeString(cachedRegistryStore, RegistryPrettyPrint.prettyPrint(registryStore));
        }
        SteamGameMetadata gameMetadata = steamRegistry.getGameMetadata(gameId);
        return new SteamGame(gameMetadata, registryStore);
    }

    public List<SteamGame> loadInstalledGames() {
        return steamRegistry.getInstalledAppIds().stream().map(this::loadGame).collect(Collectors.toList());
    }

    private boolean accurateEnoughCache(Path cachedRegistryStore) {
        long lastModified = cachedRegistryStore.toFile().lastModified();
        return cachedRegistryStore.toFile().exists() && getDaysSinceModification(lastModified) < 30;
    }

    private long getDaysSinceModification(long lastModified) {
        LocalDateTime localLastModified = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified), ZoneId.systemDefault());
        return Math.abs(ChronoUnit.DAYS.between(LocalDateTime.now(), localLastModified));
    }
}
