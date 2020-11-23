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
    public static List<SteamGame> loadInstalledGames(Path configDirectory) {
        List<SteamGame> steamGames = Lists.newArrayList();

        List<SteamGameMetadata> gameMetadata = SteamRegistry.getInstance().getGameMetadata();
        PrintAppInfo printAppInfo = new PrintAppInfo(getCacheDirectory(configDirectory));
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

    private static Path getCacheDirectory(Path configDirectory) {
        return Path.of(configDirectory.toAbsolutePath().toString(), "/cache");
    }
}
