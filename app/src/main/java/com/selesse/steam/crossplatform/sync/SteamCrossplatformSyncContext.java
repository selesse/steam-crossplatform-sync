package com.selesse.steam.crossplatform.sync;

import com.selesse.steam.SteamAccountId;
import com.selesse.steam.SteamApp;
import com.selesse.steam.SteamAppLoader;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steam.games.InstalledGameFinderService;
import com.selesse.steam.user.SteamAccountIdFinder;
import com.selesse.steamcrossplatformsync.gamesessions.GameSessionRepository;
import java.util.List;

public class SteamCrossplatformSyncContext {
    private final SteamCrossplatformSyncConfig config;
    private final InstalledGameFinderService installedGameFinderService;
    private final SteamAccountId steamAccountId;

    // Built on first use, not up front: opening it migrates the schema, and the reporting
    // commands (--print-games and friends) never record a session at all. They shouldn't pay to
    // stand up a database they don't touch.
    private GameSessionRepository sessionRepository;

    public SteamCrossplatformSyncContext() {
        this.config = SteamCrossplatformSyncConfig.load();
        this.steamAccountId = SteamAccountIdFinder.findIfPresent().orElse(null);
        this.installedGameFinderService = new InstalledGameFinderService();
    }

    public SteamCrossplatformSyncConfig getConfig() {
        return config;
    }

    public synchronized GameSessionRepository getSessionRepository() {
        if (sessionRepository == null) {
            sessionRepository = new GameSessionRepository();
        }
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
