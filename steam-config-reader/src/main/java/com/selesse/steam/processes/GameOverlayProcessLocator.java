package com.selesse.steam.processes;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.selesse.os.OperatingSystems;
import java.io.File;
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
                    return processArguments.size() > 0
                            && processArguments.get(processArguments.size() - 1).equalsIgnoreCase(gameOverlayProcess);
                })
                .findFirst();
    }

    private static String getGameOverlayProcessName() {
        return switch (OperatingSystems.get()) {
            case WINDOWS -> "GameOverlayUI.exe";
            case MAC, LINUX -> "gameoverlayui";
            case STEAM_OS -> "reaper";
        };
    }
}
