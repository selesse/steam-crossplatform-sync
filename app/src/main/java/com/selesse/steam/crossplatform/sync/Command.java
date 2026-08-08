package com.selesse.steam.crossplatform.sync;

import com.selesse.steam.crossplatform.sync.daemon.Daemon;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * Every command this program can run - exactly one per invocation - and every flag it accepts.
 * {@link #usage()} is generated from this table, so what's advertised can't drift from what
 * {@link Invocation#parse} actually allows.
 *
 * <p>The context arrives as a supplier because building it detects the Steam install and reads the
 * config file. Only commands that go on to use it should pay for that.
 */
enum Command {
    /** The default, when no command flag is given at all. */
    DAEMON(null, AppIds.NONE, "watches for a running game and syncs its save files when the game closes") {
        @Override
        void run(Supplier<SteamCrossplatformSyncContext> context, Invocation invocation) {
            new Daemon(context.get(), invocation.fast()).run();
        }
    },
    SYNC("--sync", AppIds.OPTIONAL, "Sync save files for the given games, or for every game in games.yml") {
        @Override
        void run(Supplier<SteamCrossplatformSyncContext> context, Invocation invocation) {
            SyncGameFilesService syncService = new SyncGameFilesService(context.get());
            if (invocation.appIds().isEmpty()) {
                syncService.runForAllGames();
            } else {
                syncService.run(invocation.appIdArray());
            }
        }
    },
    PRINT_GAMES("--print-games", AppIds.NONE, "Print every installed game with the save paths resolved for it") {
        @Override
        void run(Supplier<SteamCrossplatformSyncContext> context, Invocation invocation) {
            new GamesFilePrinter(context.get()).run();
        }
    },
    PRINT_GAME("--print-game", AppIds.REQUIRED, "Print the save paths resolved for specific games") {
        @Override
        void run(Supplier<SteamCrossplatformSyncContext> context, Invocation invocation) {
            new GamesFilePrinter(context.get()).run(invocation.appIdArray());
        }
    },
    PRINT_APP_CACHE(
            "--print-app-cache", AppIds.OPTIONAL, "Dump the raw app cache entry for the given apps, or for every app") {
        @Override
        void run(Supplier<SteamCrossplatformSyncContext> context, Invocation invocation) {
            AppCachePrinter printer = new AppCachePrinter(context.get().getSteamInstall());
            if (invocation.appIds().isEmpty()) {
                printer.run();
            } else {
                printer.run(invocation.appIdArray());
            }
        }
    },
    LIST_APP_IDS("--list-app-ids", AppIds.NONE, "List every app ID in the app cache alongside its name") {
        @Override
        void run(Supplier<SteamCrossplatformSyncContext> context, Invocation invocation) {
            new AppCachePrinter(context.get().getSteamInstall()).listIds();
        }
    },
    GENERATE_GAMES(
            "--generate-games",
            AppIds.NONE,
            "Print a games.yml covering the installed games whose save paths resolve") {
        @Override
        void run(Supplier<SteamCrossplatformSyncContext> context, Invocation invocation) {
            new GamesFileGenerator(context.get()).run();
        }
    },
    FIND_UNRESOLVED_SAVE_PATHS(
            "--find-unresolved-save-paths",
            AppIds.NONE,
            "Report installed games whose save paths can't be resolved, with their raw ufs block") {
        @Override
        void run(Supplier<SteamCrossplatformSyncContext> context, Invocation invocation) {
            new FindUnresolvedSavePaths(context.get()).run();
        }
    },
    HELP("--help", AppIds.NONE, "Print this message") {
        @Override
        void run(Supplier<SteamCrossplatformSyncContext> context, Invocation invocation) {
            System.out.println(usage());
        }
    };

    /** Modifies the daemon rather than selecting a command, so it isn't one of the values above. */
    static final String FAST_FLAG = "--fast";

    private static final String FAST_DESCRIPTION = "Poll every 5 seconds instead of 30 (daemon only)";

    private final @Nullable String flag;
    private final AppIds appIds;
    private final String description;

    Command(@Nullable String flag, AppIds appIds, String description) {
        this.flag = flag;
        this.appIds = appIds;
        this.description = description;
    }

    abstract void run(Supplier<SteamCrossplatformSyncContext> context, Invocation invocation);

    static Optional<Command> fromFlag(String flag) {
        return Arrays.stream(values())
                .filter(command -> flag.equals(command.flag))
                .findFirst();
    }

    /** How to name this command in an error message. */
    String displayName() {
        return flag == null ? "the daemon" : flag;
    }

    void checkAppIds(List<Long> givenAppIds) {
        if (appIds == AppIds.REQUIRED && givenAppIds.isEmpty()) {
            throw new UsageException(displayName() + " requires at least one app ID");
        }
        if (appIds == AppIds.NONE && !givenAppIds.isEmpty()) {
            throw new UsageException(displayName() + " takes no app IDs");
        }
    }

    static String usage() {
        List<Command> commands = Arrays.stream(values())
                .filter(command -> command.flag != null && command != HELP)
                .toList();
        int width = Math.max(
                commands.stream()
                        .mapToInt(command -> command.invocationText().length())
                        .max()
                        .orElse(0),
                Math.max(FAST_FLAG.length(), HELP.invocationText().length()));

        StringBuilder usage = new StringBuilder()
                .append("Usage: steam-crossplatform-sync [command] [app ID...]\n")
                .append("\n")
                .append("With no command, runs the daemon: it ")
                .append(DAEMON.description)
                .append(".\n")
                .append("\n")
                .append("Commands:\n");
        commands.forEach(command -> usage.append(row(command.invocationText(), command.description, width)));
        return usage.append("\n")
                .append("Options:\n")
                .append(row(FAST_FLAG, FAST_DESCRIPTION, width))
                .append(row(HELP.invocationText(), HELP.description, width))
                .toString();
    }

    private String invocationText() {
        return flag == null ? "" : flag + appIds.usage();
    }

    private static String row(String invocation, String description, int width) {
        return "  " + invocation + " ".repeat(width - invocation.length()) + "  " + description + "\n";
    }

    /** Whether a command takes Steam app IDs as positional arguments. */
    private enum AppIds {
        NONE(""),
        OPTIONAL(" [app ID...]"),
        REQUIRED(" <app ID...>");

        private final String usage;

        AppIds(String usage) {
            this.usage = usage;
        }

        String usage() {
            return usage;
        }
    }
}
