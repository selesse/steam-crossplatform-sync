package com.selesse.steam.crossplatform.sync;

import com.google.common.base.Joiner;
import com.selesse.os.OperatingSystems.OperatingSystem;
import com.selesse.steam.SteamApp;
import com.selesse.steam.games.UserFileSystemPath;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GamesFilePrinter {
    private static final List<OperatingSystem> PRINTED_OSES =
            List.of(OperatingSystem.WINDOWS, OperatingSystem.MAC, OperatingSystem.LINUX);

    private final SteamCrossplatformSyncContext context;

    public GamesFilePrinter(SteamCrossplatformSyncContext context) {
        this.context = context;
    }

    public void run() {
        List<SteamApp> steamApps = new ArrayList<>(context.fetchAllGamesOrLoadInstalledGames());
        steamApps.sort(Comparator.comparing(SteamApp::getName));
        steamApps.forEach(this::printApp);
    }

    public void run(Long... gameIds) {
        for (Long gameId : gameIds) {
            printApp(context.loadGame(gameId));
        }
    }

    private void printApp(SteamApp steamApp) {
        if (!steamApp.isGame()) {
            return;
        }
        System.out.println(steamApp);
        System.out.println("  Supported OSes: " + Joiner.on(", ").join(steamApp.getSupportedOperatingSystems()));

        if (!steamApp.hasUserFileSystem()) {
            System.out.println("  No save data found");
            System.out.println("");
            return;
        }

        boolean printedAnyPath = false;
        for (OperatingSystem os : PRINTED_OSES) {
            for (UserFileSystemPath path : savePathsOrEmpty(steamApp, os)) {
                System.out.println("  " + label(os) + " path: " + path.getSymbolPath());
                printedAnyPath = true;
            }
        }
        if (!printedAnyPath) {
            System.out.println("  Did not compute installation path for " + steamApp);
        }
        System.out.println("");
    }

    // Printing is best-effort: one OS whose save paths can't be resolved shouldn't stop us from
    // reporting the ones that can.
    private List<UserFileSystemPath> savePathsOrEmpty(SteamApp steamApp, OperatingSystem os) {
        try {
            return steamApp.getSavePaths(os);
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    private String label(OperatingSystem os) {
        return switch (os.family()) {
            case WINDOWS -> "Windows";
            case MAC -> "Mac";
            case LINUX -> "Linux";
        };
    }
}
