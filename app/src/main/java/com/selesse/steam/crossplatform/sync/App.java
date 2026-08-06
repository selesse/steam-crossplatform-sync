package com.selesse.steam.crossplatform.sync;

import com.selesse.steam.crossplatform.sync.daemon.Daemon;
import java.util.List;

public class App {
    public static void main(String[] args) {
        List<String> arguments = List.of(args);

        SteamCrossplatformSyncContext context = new SteamCrossplatformSyncContext();
        if (arguments.contains("--sync")) {
            Long[] gameIds = idsAfter(arguments, "--sync");
            if (gameIds.length == 0) {
                new SyncGameFilesService(context).runForAllGames();
            } else {
                new SyncGameFilesService(context).run(gameIds);
            }
        } else if (arguments.contains("--print-games")) {
            new GamesFilePrinter(context).run();
        } else if (arguments.contains("--print-game")) {
            new GamesFilePrinter(context).run(idsAfter(arguments, "--print-game"));
        } else if (arguments.contains("--print-app-cache")) {
            Long[] appIds = idsAfter(arguments, "--print-app-cache");
            if (appIds.length == 0) {
                new AppCachePrinter(context.getSteamInstall()).run();
            } else {
                new AppCachePrinter(context.getSteamInstall()).run(appIds);
            }
        } else if (arguments.contains("--list-app-ids")) {
            new AppCachePrinter(context.getSteamInstall()).listIds();
        } else if (arguments.contains("--generate-games")) {
            new GamesFileGenerator(context).run();
        } else if (arguments.contains("--find-unresolved-save-paths")) {
            new FindUnresolvedSavePaths(context).run();
        } else {
            boolean fast = arguments.contains("--fast");
            new Daemon(context, fast).run();
        }
    }

    private static Long[] idsAfter(List<String> arguments, String flag) {
        return arguments.subList(arguments.indexOf(flag) + 1, arguments.size()).stream()
                .map(Long::parseLong)
                .toArray(Long[]::new);
    }
}
