package com.selesse.steam.crossplatform.sync;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.games.SteamGame;
import com.selesse.steam.games.UserFileSystemPath;
import com.selesse.steam.registry.RegistryPrettyPrint;
import com.selesse.steam.registry.implementation.RegistryObject;
import java.util.List;

public class FindUndetectedSaveFiles {
    private final SteamCrossplatformSyncContext context;

    public FindUndetectedSaveFiles(SteamCrossplatformSyncContext context) {
        this.context = context;
    }

    public void run() {
        List<SteamGame> steamGames = context.fetchAllGamesOrLoadInstalledGames();
        for (SteamGame steamGame : steamGames) {
            if (steamGame.hasUserCloud()) {
                boolean isFullyIntegrated = true;
                List<OperatingSystems.OperatingSystem> operatingSystems = steamGame.supportedOperatingSystems();
                for (OperatingSystems.OperatingSystem operatingSystem : operatingSystems) {
                    try {
                        List<UserFileSystemPath> installationPaths = steamGame.getInstallationPaths(operatingSystem);
                        if (installationPaths.isEmpty()) {
                            throw new RuntimeException("Did not find installation path for OS " + operatingSystem);
                        }
                    } catch (RuntimeException e) {
                        isFullyIntegrated = false;
                        System.out.println(
                                steamGame.getName() + " => " + operatingSystem + " - installation path not found");
                    }
                }

                if (!isFullyIntegrated) {
                    System.out.println("");
                    RegistryObject ufs = steamGame.getRegistryStore().getObjectValueAsObject("ufs");
                    System.out.println(RegistryPrettyPrint.prettyPrint(ufs));
                    System.out.println("");
                }
            }
        }
    }
}
