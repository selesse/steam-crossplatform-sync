package com.selesse.steam.crossplatform.sync;

import com.selesse.steam.GameRegistries;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steam.games.SteamGame;
import com.selesse.steam.games.SteamGameMetadata;
import com.selesse.steam.registry.SteamRegistry;
import com.selesse.steam.registry.implementation.RegistryStore;

import java.util.List;
import java.util.stream.Collectors;

public class GameLoadingService {
    private final GameRegistries gameRegistries;
    private final SteamRegistry steamRegistry;

    public GameLoadingService(SteamCrossplatformSyncConfig config) {
        if (config.getRemoteAppInfoUrl() != null) {
            gameRegistries = GameRegistries.buildWithRemoteFallback(config.getRemoteAppInfoUrl());
        } else {
            gameRegistries = GameRegistries.build();
        }
        steamRegistry = SteamRegistry.getInstance();
    }

    public SteamGame loadGame(long gameId) {
        RegistryStore registryStore = gameRegistries.load(gameId);
        SteamGameMetadata gameMetadata = steamRegistry.getGameMetadata(gameId);
        return new SteamGame(gameMetadata, registryStore);
    }

    public List<SteamGame> loadGames() {
        return steamRegistry.getInstalledAppIds().stream().map(this::loadGame).collect(Collectors.toList());
    }
}
