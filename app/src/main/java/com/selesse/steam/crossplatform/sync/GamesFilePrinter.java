package com.selesse.steam.crossplatform.sync;

import com.google.common.base.Joiner;
import com.selesse.steam.games.SteamGame;

import java.util.List;

;

public class GamesFilePrinter {
    private final SteamCrossplatformSyncContext context;

    public GamesFilePrinter(SteamCrossplatformSyncContext context) {
        this.context = context;
    }

    public void run() {
        List<SteamGame> steamGames = context.loadGames();
        for (SteamGame steamGame : steamGames) {
            printSteamGame(steamGame);
        }
    }

    public void run(Long... gameIds) {
        for (Long gameId : gameIds) {
            SteamGame steamGame = context.loadGame(gameId);
            printSteamGame(steamGame);
        }
    }

    private void printSteamGame(SteamGame steamGame) {
        if (steamGame.isGame()) {
            System.out.println(steamGame.metadata());
            System.out.println("  Supported OSes: " + Joiner.on(", ").join(steamGame.supportedOperatingSystems()));
            if (steamGame.hasUserCloud()) {
                if (steamGame.hasWindowsPath()) {
                    System.out.println("  Windows path: " + steamGame.getWindowsInstallationPath());
                }
                if (steamGame.hasMacPath()) {
                    System.out.println("  Mac path: " + steamGame.getMacInstallationPath());
                }
                if (steamGame.hasLinuxPath()) {
                    System.out.println("  Linux path: " + steamGame.getLinuxInstallationPath());
                }
                if (!steamGame.hasComputedInstallationPath()) {
                    System.out.println("  Did not compute installation path for " + steamGame);
                }
            }
            System.out.println("");
        }
    }
}
