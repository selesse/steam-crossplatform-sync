package com.selesse.steam.crossplatform.sync;

import com.selesse.steam.Games;
import com.selesse.steam.steamcmd.SteamGame;

import java.util.List;

public class GamesFilePrinter {
    public void run() {
        List<SteamGame> steamGames = Games.loadInstalledGames();
        for (SteamGame steamGame : steamGames) {
            String installed = steamGame.isInstalled() ? "installed" : "not installed";
            System.out.println(steamGame.getName() + " (" + steamGame.getId() + ") " + installed);
            if (steamGame.hasUserCloud()) {
                try {
                    System.out.println("Windows path: " + steamGame.getWindowsInstallationPath());
                    System.out.println("Mac path: " + steamGame.getMacInstallationPath());
                } catch (RuntimeException e) {
                    System.err.println("Unable to install locations for " + steamGame.getName());
                }
            }
        }
    }
}
