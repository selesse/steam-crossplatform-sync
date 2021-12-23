package com.selesse.steam;

import com.google.common.collect.Lists;
import com.selesse.steam.registry.SteamRegistry;
import com.selesse.steam.registry.implementation.RegistryStore;
import com.selesse.steam.steamcmd.PrintAppInfo;
import com.selesse.steam.steamcmd.SteamGame;
import com.selesse.steam.steamcmd.games.SteamGameConfig;
import com.selesse.steam.steamcmd.games.SteamGameMetadata;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Games {
    public static SteamGame loadGame(Path cacheDirectory, String remoteAppInfoUrl, Long gameId) {
        PrintAppInfo printAppInfo = new PrintAppInfo(cacheDirectory, remoteAppInfoUrl);
        RegistryStore registryStore = printAppInfo.getRegistryStore(gameId);
        SteamGameMetadata metadata = SteamRegistry.getInstance().getGameMetadata(gameId);
        return new SteamGame(metadata, new SteamGameConfig(registryStore));
    }

    public static List<SteamGame> loadInstalledGames(Path cacheDirectory, String remoteAppInfoUrl) {
        List<SteamGame> steamGames = Lists.newArrayList();

        List<SteamGameMetadata> gameMetadata = SteamRegistry.getInstance().getGameMetadata();
        PrintAppInfo printAppInfo = new PrintAppInfo(cacheDirectory, remoteAppInfoUrl);
        Map<Long, RegistryStore> registryStores =
                printAppInfo.getRegistryStores(
                        gameMetadata.stream().map(SteamGameMetadata::getGameId).collect(Collectors.toList())
               );

        for (SteamGameMetadata metadata : gameMetadata) {
            RegistryStore registryStore = registryStores.get(metadata.getGameId());

            steamGames.add(new SteamGame(metadata, new SteamGameConfig(registryStore)));
        }
        return steamGames;
    }
}
