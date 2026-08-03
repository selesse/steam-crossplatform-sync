package com.selesse.steam.crossplatform.sync;

import com.google.common.base.Joiner;
import com.selesse.os.OperatingSystems.OperatingSystem;
import com.selesse.steam.games.SteamGame;
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
        List<SteamGame> steamGames = new ArrayList<>(context.fetchAllGamesOrLoadInstalledGames());
        steamGames.sort(Comparator.comparing(SteamGame::getName));
        steamGames.forEach(this::printSteamGame);
    }

    public void run(Long... gameIds) {
        for (Long gameId : gameIds) {
            printSteamGame(context.loadGame(gameId));
        }
    }

    private void printSteamGame(SteamGame steamGame) {
        if (!steamGame.isGame()) {
            return;
        }
        System.out.println(steamGame.metadata());
        System.out.println("  Supported OSes: " + Joiner.on(", ").join(steamGame.supportedOperatingSystems()));

        if (!steamGame.hasUserCloud()) {
            System.out.println("  No save data found");
            System.out.println("");
            return;
        }

        boolean printedAnyPath = false;
        for (OperatingSystem os : PRINTED_OSES) {
            for (UserFileSystemPath path : savePathsOrEmpty(steamGame, os)) {
                System.out.println("  " + label(os) + " path: " + path.getSymbolPath());
                printedAnyPath = true;
            }
        }
        if (!printedAnyPath) {
            System.out.println("  Did not compute installation path for " + steamGame);
        }
        System.out.println("");
    }

    // Printing is best-effort: one OS whose save paths can't be resolved shouldn't stop us from
    // reporting the ones that can.
    private List<UserFileSystemPath> savePathsOrEmpty(SteamGame steamGame, OperatingSystem os) {
        try {
            return steamGame.getSavePaths(os);
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    private String label(OperatingSystem os) {
        return switch (os) {
            case WINDOWS -> "Windows";
            case MAC -> "Mac";
            case LINUX, STEAM_OS -> "Linux";
        };
    }
}
