package com.selesse.steam.crossplatform.sync;

import com.selesse.steam.SteamAccountId;
import com.selesse.steam.SteamApp;
import com.selesse.steam.SteamAppLoader;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSync;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steam.games.InstalledGameFinderService;
import com.selesse.steam.user.SteamAccountIdFinder;
import com.selesse.steamcrossplatformsync.gamesessions.GameSessionRepository;
import java.util.List;

public class SteamCrossplatformSyncContext {
    private final SteamCrossplatformSyncConfig config;
    private final InstalledGameFinderService installedGameFinderService;
    private final SteamAccountId steamAccountId;
    private final GameSessionRepository sessionRepository;

    public SteamCrossplatformSyncContext() {
        this.config = SteamCrossplatformSync.loadConfiguration();
        this.steamAccountId = SteamAccountIdFinder.findIfPresent().orElse(null);
        this.installedGameFinderService = new InstalledGameFinderService();
        this.sessionRepository = new GameSessionRepository();
    }

    public SteamCrossplatformSyncConfig getConfig() {
        return config;
    }

    public GameSessionRepository getSessionRepository() {
        return sessionRepository;
    }

    public SteamApp loadGame(long gameId) {
        return SteamAppLoader.load(gameId);
    }

    public List<SteamApp> fetchAllGamesOrLoadInstalledGames() {
        return installedGameFinderService.find().stream().map(this::loadGame).toList();
    }

    public SteamAccountId getSteamAccountIdIfPresent() {
        return steamAccountId;
    }
}
