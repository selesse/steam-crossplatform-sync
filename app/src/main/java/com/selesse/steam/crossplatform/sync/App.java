package com.selesse.steam.crossplatform.sync;

import com.google.common.collect.Lists;
import com.selesse.steam.crossplatform.sync.daemon.Daemon;
import com.selesse.steam.crossplatform.sync.server.AppInfoServer;

import java.util.Arrays;
import java.util.List;

public class App {
    public static void main(String[] args) {
        List<String> arguments = Lists.newArrayList(args);

        SteamCrossplatformSyncContext context = new SteamCrossplatformSyncContext();
        if (arguments.contains("--sync")) {
            int index = arguments.indexOf("--sync");
            List<String> argList = Arrays.stream(args).toList();
            if (argList.size() == 1) {
                new SyncGameFilesService(context).runForAllGames();
            } else {
                Long[] gameIds = argList.subList(index + 1, args.length).stream().map(Long::parseLong).toArray(Long[]::new);
                new SyncGameFilesService(context).run(gameIds);
            }
        } else if (arguments.contains("--print-games")) {
            new GamesFilePrinter(context).run();
        } else if (arguments.contains("--print-game")) {
            int index = arguments.indexOf("--print-game");
            List<String> argList = Arrays.stream(args).toList();
            Long[] gameIds = argList.subList(index + 1, args.length).stream().map(Long::parseLong).toArray(Long[]::new);
            new GamesFilePrinter(context).run(gameIds);
        } else if (arguments.contains("--generate-games")) {
            new GamesFileGenerator(context).run();
        } else if (arguments.contains("--app-info-server")) {
            new AppInfoServer(context.getConfig()).run();
        } else if (arguments.contains("--find-unhandled-save-files")) {
            new FindUndetectedSaveFiles(context).run();
        } else {
            boolean fast = arguments.contains("--fast");
            new Daemon(context, fast).run();
        }
    }
}
