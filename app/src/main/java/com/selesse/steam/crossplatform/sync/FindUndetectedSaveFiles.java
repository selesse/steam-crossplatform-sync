package com.selesse.steam.crossplatform.sync;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.SteamApp;
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
        List<SteamApp> steamApps = context.fetchAllGamesOrLoadInstalledGames();
        for (SteamApp steamApp : steamApps) {
            if (steamApp.hasUserFileSystem()) {
                boolean isFullyIntegrated = true;
                List<OperatingSystems.OperatingSystem> operatingSystems = steamApp.getSupportedOperatingSystems();
                for (OperatingSystems.OperatingSystem operatingSystem : operatingSystems) {
                    try {
                        List<UserFileSystemPath> installationPaths = steamApp.getSavePaths(operatingSystem);
                        if (installationPaths.isEmpty()) {
                            throw new RuntimeException("Did not find installation path for OS " + operatingSystem);
                        }
                    } catch (RuntimeException e) {
                        isFullyIntegrated = false;
                        System.out.println(
                                steamApp.getName() + " => " + operatingSystem + " - installation path not found");
                    }
                }

                if (!isFullyIntegrated) {
                    System.out.println("");
                    RegistryObject ufs = steamApp.getRegistryObject().getObjectValueAsObject("ufs");
                    System.out.println(RegistryPrettyPrint.prettyPrint(ufs));
                    System.out.println("");
                }
            }
        }
    }
}
