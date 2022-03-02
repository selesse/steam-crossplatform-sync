package com.selesse.steam.crossplatform.sync;

import com.selesse.steam.SteamAccountId;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSync;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steam.games.SteamGame;
import com.selesse.steam.user.SteamAccountIdFinder;
import java.util.List;

public class SteamCrossplatformSyncContext {
    private final SteamCrossplatformSyncConfig config;
    private final GameLoadingService gameLoadingService;
    private final SteamAccountId steamAccountId;

    public SteamCrossplatformSyncContext() {
        this.config = SteamCrossplatformSync.loadConfiguration();
        this.gameLoadingService = new GameLoadingService(config);
        this.steamAccountId = SteamAccountIdFinder.findIfPresent().orElse(null);
    }

    public SteamCrossplatformSyncConfig getConfig() {
        return config;
    }

    public SteamGame loadGame(long gameId) {
        return gameLoadingService.loadGame(gameId);
    }

    public List<SteamGame> fetchAllGamesOrLoadInstalledGames() {
        return gameLoadingService.fetchAllGamesOrLoadInstalledGames();
    }

    public SteamAccountId getSteamAccountIdIfPresent() {
        return steamAccountId;
    }
}
