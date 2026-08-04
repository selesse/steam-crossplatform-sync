package com.selesse.steam.processes;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.registry.windows.GetGameIdFromGameOverlay;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GameOverlayProcessLocator {
    public static Optional<ProcessHandle> locate() {
        return ProcessHandle.allProcesses()
                .filter(ProcessHandle::isAlive)
                .filter(p -> {
                    String gameOverlayProcess = getGameOverlayProcessName();
                    List<String> processArguments = p.info()
                            .command()
                            .map(x -> Splitter.on(File.separatorChar).splitToList(x))
                            .orElse(Lists.newArrayList());
                    return !processArguments.isEmpty()
                            && processArguments.getLast().equalsIgnoreCase(gameOverlayProcess);
                })
                .findFirst();
    }

    public static long getRunningAppId() {
        return switch (OperatingSystems.get().family()) {
            case WINDOWS -> locate().isPresent() ? GetGameIdFromGameOverlay.get() : 0L;
            case MAC ->
                locate().map(process -> {
                            var args = process.info().arguments().orElse(new String[] {});
                            int gameIdIndex = Arrays.asList(args).indexOf("-gameid");
                            if (gameIdIndex != -1 && args.length >= gameIdIndex + 1) {
                                return Long.parseLong(args[gameIdIndex + 1]);
                            }
                            return 0L;
                        })
                        .orElse(0L);
            case LINUX ->
                locate().map(process -> {
                            var args = process.info().arguments().orElse(new String[] {});
                            for (String arg : args) {
                                if (arg.contains("AppId=")) {
                                    return Long.parseLong(arg.split("=")[1]);
                                }
                            }
                            return 0L;
                        })
                        .orElse(0L);
        };
    }

    // The one place SteamOS genuinely differs from Linux, so this switches on the OS itself.
    private static String getGameOverlayProcessName() {
        return switch (OperatingSystems.get()) {
            case WINDOWS -> "GameOverlayUI64.exe";
            case MAC, LINUX -> "gameoverlayui";
            case STEAM_OS -> "reaper";
        };
    }
}
