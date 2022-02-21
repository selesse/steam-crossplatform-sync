package com.selesse.steam.crossplatform.sync;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.games.SteamGame;

import java.util.List;

public class FindUndetectedSaveFiles {
    private final SteamCrossplatformSyncContext context;

    public FindUndetectedSaveFiles(SteamCrossplatformSyncContext context) {
        this.context = context;
    }

    public void run() {
        List<SteamGame> steamGames = context.loadGames();
        for (SteamGame steamGame : steamGames) {
            if (steamGame.hasUserCloud()) {
                List<OperatingSystems.OperatingSystem> operatingSystems = steamGame.supportedOperatingSystems();
                for (OperatingSystems.OperatingSystem operatingSystem : operatingSystems) {
                    try {
                        steamGame.getInstallationPath(operatingSystem);
                    } catch (RuntimeException e) {
                        System.out.println(steamGame.getName() + " => " + operatingSystem + " - installation path not found");
                    }
                }
            }
        }
    }
}
