package com.selesse.steam;

import com.selesse.steam.appcache.AppCacheRegistryLoader;
import com.selesse.steam.registry.GameRegistryLoader;
import com.selesse.steam.registry.RegistryNotFoundException;
import com.selesse.steam.registry.implementation.RegistryStore;
import com.selesse.steam.steamcmd.SteamCmdRegistryLoader;
import com.selesse.steam.steamcmd.remote.RemoteGameRegistryLoader;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameRegistries {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameRegistries.class);
    private final List<GameRegistryLoader> loaders;

    private GameRegistries(List<GameRegistryLoader> loaders) {
        this.loaders = loaders;
    }

    public RegistryStore load(long gameId) {
        for (GameRegistryLoader loader : loaders) {
            try {
                return loader.load(gameId);
            } catch (RegistryNotFoundException e) {
                LOGGER.info(
                        "Could not load game {} using {}",
                        gameId,
                        loader.getClass().getSimpleName());
            }
        }
        LOGGER.error("Wasn't able to load game {} using any of the loading strategies", gameId);
        throw new RegistryNotFoundException();
    }

    public static GameRegistries buildWithRemoteFallback(String remoteAppInfoUrl) {
        return new GameRegistries(remoteFallbackLoaders(remoteAppInfoUrl));
    }

    public static GameRegistries build() {
        return new GameRegistries(defaultLoaders());
    }

    private static List<GameRegistryLoader> remoteFallbackLoaders(String remoteAppInfoUrl) {
        return List.of(
                new AppCacheRegistryLoader(),
                new SteamCmdRegistryLoader(),
                new RemoteGameRegistryLoader(remoteAppInfoUrl));
    }

    private static List<GameRegistryLoader> defaultLoaders() {
        return List.of(new AppCacheRegistryLoader(), new SteamCmdRegistryLoader());
    }
}
