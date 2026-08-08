package com.selesse.steam.crossplatform.sync;

import com.selesse.steam.SteamAccountId;
import com.selesse.steam.SteamApp;
import com.selesse.steam.SteamAppLoader;
import com.selesse.steam.SteamInstall;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steam.games.InstalledGameFinderService;
import com.selesse.steamcrossplatformsync.gamesessions.GameSessionRepository;
import java.util.List;

public class SteamCrossplatformSyncContext {
    private final SteamCrossplatformSyncConfig config;
    private final InstalledGameFinderService installedGameFinderService;
    private final SteamInstall steamInstall;
    private final SteamAppLoader steamAppLoader;

    // Built on first use, not up front: opening it migrates the schema, and the reporting
    // commands (--print-games and friends) never record a session at all. They shouldn't pay to
    // stand up a database they don't touch.
    private GameSessionRepository sessionRepository;

    public SteamCrossplatformSyncContext() {
        this.config = SteamCrossplatformSyncConfig.load();
        this.steamInstall = SteamInstall.detect();
        this.steamAppLoader = new SteamAppLoader(steamInstall);
        this.installedGameFinderService = new InstalledGameFinderService(steamInstall);
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
        return steamAppLoader.load(gameId);
    }

    public SteamInstall getSteamInstall() {
        return steamInstall;
    }

    public List<SteamApp> loadInstalledGames() {
        return steamAppLoader.loadSome(installedGameFinderService.find());
    }

    public SteamAccountId getSteamAccountIdIfPresent() {
        return steamInstall.accountId();
    }
}
