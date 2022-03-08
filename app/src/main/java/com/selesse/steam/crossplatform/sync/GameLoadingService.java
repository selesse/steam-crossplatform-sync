package com.selesse.steam.crossplatform.sync;

import com.selesse.caches.FileBackedCache;
import com.selesse.caches.FileBackedCacheBuilder;
import com.selesse.files.FileModified;
import com.selesse.steam.GameRegistries;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steam.games.SteamGame;
import com.selesse.steam.games.SteamGameMetadata;
import com.selesse.steam.registry.RegistryPrettyPrint;
import com.selesse.steam.registry.SteamRegistry;
import com.selesse.steam.registry.implementation.RegistryParser;
import com.selesse.steam.registry.implementation.RegistryStore;
import java.nio.file.Path;
import java.util.List;

public class GameLoadingService {
    private final GameRegistries gameRegistries;
    private final SteamCrossplatformSyncConfig config;

    public GameLoadingService(SteamCrossplatformSyncConfig config) {
        this.config = config;
        if (config.getRemoteAppInfoUrl() != null) {
            gameRegistries = GameRegistries.buildWithRemoteFallback(config.getRemoteAppInfoUrl());
        } else {
            gameRegistries = GameRegistries.build();
        }
    }

    public SteamGame loadGame(long gameId) {
        FileBackedCache fileBackedCache = new FileBackedCacheBuilder()
                .setCacheLoadingCriteria(this::accurateEnoughGameCache)
                .setLoadingMechanism(() -> RegistryPrettyPrint.prettyPrint(gameRegistries.load(gameId)))
                .setSuccessfulLoadCriteria(x -> x.size() > 3)
                .build();
        Path cachedRegistryStore = Path.of(config.getCacheDirectory().toString(), gameId + ".vdf");
        RegistryStore registryStore = RegistryParser.parse(fileBackedCache.getLines(cachedRegistryStore));
        SteamGameMetadata gameMetadata = SteamRegistry.getInstance().getGameMetadata(gameId);
        return new SteamGame(gameMetadata, registryStore);
    }

    public List<SteamGame> loadGames(List<Long> gameIds) {
        return gameIds.stream().map(this::loadGame).toList();
    }

    private boolean accurateEnoughGameCache(Path path) {
        return path.toFile().exists() && FileModified.getDaysSinceModification(path.toFile()) < 30;
    }
}
