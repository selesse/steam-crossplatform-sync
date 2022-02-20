package com.selesse.steam.crossplatform.sync;

import com.selesse.steam.Games;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steam.steamcmd.SteamGame;

import java.util.List;

public class GamesFilePrinter {
    private final SteamCrossplatformSyncConfig config;

    public GamesFilePrinter(SteamCrossplatformSyncConfig config) {
        this.config = config;
    }

    public void run() {
        List<SteamGame> steamGames = Games.loadInstalledGames(config.getConfigDirectory(), config.getRemoteAppInfoUrl());
        for (SteamGame steamGame : steamGames) {
            printSteamGame(steamGame);
        }
    }

    public void run(Long... gameIds) {
        for (Long gameId : gameIds) {
            SteamGame steamGame = Games.loadGame(config.getCacheDirectory(), config.getRemoteAppInfoUrl(), gameId);
            printSteamGame(steamGame);
        }
    }

    private void printSteamGame(SteamGame steamGame) {
        if (steamGame.isGame()) {
            System.out.println(steamGame.metadata());
            if (steamGame.hasUserCloud()) {
                if (steamGame.hasComputedInstallationPath()) {
                    System.out.println("Windows path: " + steamGame.getWindowsInstallationPath());
                    System.out.println("Mac path: " + steamGame.getMacInstallationPath());
                } else {
                    System.out.println("Did not compute installation path for " + steamGame);
                }
            }
        }
    }
}
