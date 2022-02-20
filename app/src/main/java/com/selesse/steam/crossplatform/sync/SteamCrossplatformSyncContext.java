package com.selesse.steam.crossplatform.sync;

import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSync;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steam.games.SteamGame;

import java.util.List;

public class SteamCrossplatformSyncContext {
    private final SteamCrossplatformSyncConfig config;
    private final GameLoadingService gameLoadingService;

    public SteamCrossplatformSyncContext() {
        this.config = SteamCrossplatformSync.loadConfiguration();
        this.gameLoadingService = new GameLoadingService(config);
    }

    public SteamCrossplatformSyncConfig getConfig() {
        return config;
    }

    public SteamGame loadGame(long gameId) {
        return gameLoadingService.loadGame(gameId);
    }

    public List<SteamGame> loadGames() {
        return gameLoadingService.loadGames();
    }
}
