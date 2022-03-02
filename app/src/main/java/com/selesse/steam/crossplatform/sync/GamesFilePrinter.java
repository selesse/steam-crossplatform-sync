package com.selesse.steam.crossplatform.sync;

import com.google.common.base.Joiner;
import com.selesse.steam.games.SteamGame;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GamesFilePrinter {
    private final SteamCrossplatformSyncContext context;

    public GamesFilePrinter(SteamCrossplatformSyncContext context) {
        this.context = context;
    }

    public void run() {
        List<SteamGame> steamGames = new ArrayList<>(context.fetchAllGamesOrLoadInstalledGames());
        steamGames.sort(Comparator.comparing(SteamGame::getName));
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
                    steamGame
                            .getWindowsInstallationPaths()
                            .forEach(path -> System.out.println("  Windows path: " + path.getSymbolPath()));
                }
                if (steamGame.hasMacPath()) {
                    steamGame
                            .getMacInstallationPaths()
                            .forEach(path -> System.out.println("  Mac path: " + path.getSymbolPath()));
                }
                if (steamGame.hasLinuxPath()) {
                    steamGame
                            .getLinuxInstallationPaths()
                            .forEach(path -> System.out.println("  Linux path: " + path.getSymbolPath()));
                }
                if (!steamGame.hasComputedInstallationPath()) {
                    System.out.println("  Did not compute installation path for " + steamGame);
                }
            } else {
                System.out.println("  No save data found");
            }
            System.out.println("");
        }
    }
}
