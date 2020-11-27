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
        List<SteamGame> steamGames = Games.loadInstalledGames(config.getConfigDirectory());
        for (SteamGame steamGame : steamGames) {
            printSteamGame(steamGame);
        }
    }

    public void run(Long... gameIds) {
        for (Long gameId : gameIds) {
            SteamGame steamGame = Games.loadGame(config.getConfigDirectory(), gameId);
            printSteamGame(steamGame);
        }
    }

    private void printSteamGame(SteamGame steamGame) {
        if (steamGame.isGame()) {
            String installed = steamGame.isInstalled() ? "installed" : "not installed";
            System.out.println(steamGame.getName() + " (" + steamGame.getId() + ") " + installed);
            if (steamGame.hasUserCloud()) {
                try {
                    System.out.println("Windows path: " + steamGame.getWindowsInstallationPath());
                    System.out.println("Mac path: " + steamGame.getMacInstallationPath());
                } catch (RuntimeException e) {
                    System.err.println("Unable to print install locations for " + steamGame.getName());
                }
            }
        }
    }
}
