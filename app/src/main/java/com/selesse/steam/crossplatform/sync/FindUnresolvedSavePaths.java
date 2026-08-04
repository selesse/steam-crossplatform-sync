package com.selesse.steam.crossplatform.sync;

import com.selesse.os.OperatingSystems.OperatingSystem;
import com.selesse.steam.SteamApp;
import com.selesse.steam.registry.RegistryPrettyPrint;
import java.util.List;
import java.util.Optional;

/**
 * Reports installed games whose ufs block the save-path parser can't resolve, dumping the raw
 * block alongside each one. That dump is the starting point for teaching the parser a new shape
 * and adding the game to game-installation-paths.yml.
 */
public class FindUnresolvedSavePaths {
    private final SteamCrossplatformSyncContext context;

    public FindUnresolvedSavePaths(SteamCrossplatformSyncContext context) {
        this.context = context;
    }

    public void run() {
        for (SteamApp steamApp : context.fetchAllGamesOrLoadInstalledGames()) {
            if (steamApp.hasUserFileSystem()) {
                report(steamApp);
            }
        }
    }

    private void report(SteamApp steamApp) {
        List<String> failures = steamApp.getSupportedOperatingSystems().stream()
                .flatMap(os ->
                        resolutionFailure(steamApp, os)
                                .map(failure -> steamApp.getName() + " => " + os + " - " + failure)
                                .stream())
                .toList();
        if (failures.isEmpty()) {
            return;
        }

        failures.forEach(System.out::println);
        System.out.println("");
        System.out.println(
                RegistryPrettyPrint.prettyPrint(steamApp.getRegistryObject().getObjectValueAsObject("ufs")));
        System.out.println("");
    }

    /**
     * Empty when the paths resolved. Resolving to nothing and blowing up are different gaps, and
     * the parser's own message is the useful half of the second, so they aren't folded together.
     */
    private Optional<String> resolutionFailure(SteamApp steamApp, OperatingSystem os) {
        try {
            return steamApp.getSavePaths(os).isEmpty() ? Optional.of("no save path resolved") : Optional.empty();
        } catch (RuntimeException e) {
            return Optional.of(e.toString());
        }
    }
}
