package com.selesse.steam.crossplatform.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A parsed command line: which command to run, and with what.
 *
 * <p>Parsing is strict on purpose. Anything unrecognized used to fall through to the daemon, so a
 * mistyped flag silently started a background process instead of doing what was asked.
 */
record Invocation(Command command, List<Long> appIds, boolean fast) {
    Invocation {
        appIds = List.copyOf(appIds);
    }

    static Invocation parse(List<String> arguments) {
        Command command = null;
        List<Long> appIds = new ArrayList<>();
        boolean fast = false;

        for (String argument : arguments) {
            if (Command.FAST_FLAG.equals(argument)) {
                fast = true;
            } else if (argument.startsWith("-")) {
                Command match =
                        Command.fromFlag(argument).orElseThrow(() -> new UsageException("Unknown option " + argument));
                if (command != null && command != match) {
                    throw new UsageException(
                            "Only one command at a time, got " + command.displayName() + " and " + match.displayName());
                }
                command = match;
            } else {
                appIds.add(parseAppId(argument));
            }
        }

        if (command == null) {
            // App IDs alone don't say what to do with them, and guessing is how you end up syncing
            // when someone meant to print.
            if (!appIds.isEmpty()) {
                throw new UsageException("No command given for app ID " + appIds.getFirst());
            }
            command = Command.DAEMON;
        }
        command.checkAppIds(appIds);
        if (fast && command != Command.DAEMON) {
            throw new UsageException(Command.FAST_FLAG + " sets the daemon's polling interval, so it can't be "
                    + "combined with " + command.displayName());
        }
        return new Invocation(command, appIds, fast);
    }

    void run(Supplier<SteamCrossplatformSyncContext> context) {
        command.run(context, this);
    }

    Long[] appIdArray() {
        return appIds.toArray(Long[]::new);
    }

    private static long parseAppId(String argument) {
        try {
            return Long.parseLong(argument);
        } catch (NumberFormatException e) {
            throw new UsageException("Not a Steam app ID: " + argument);
        }
    }
}
