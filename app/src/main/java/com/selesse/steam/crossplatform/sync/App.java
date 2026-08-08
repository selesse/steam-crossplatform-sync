package com.selesse.steam.crossplatform.sync;

import java.util.List;

public class App {
    private static final int USAGE_EXIT_CODE = 2;

    public static void main(String[] args) {
        Invocation invocation;
        try {
            invocation = Invocation.parse(List.of(args));
        } catch (UsageException e) {
            System.err.println(e.getMessage());
            System.err.println();
            System.err.print(Command.usage());
            System.exit(USAGE_EXIT_CODE);
            return;
        }
        invocation.run(SteamCrossplatformSyncContext::new);
    }
}
