package com.selesse.steam.registry.windows;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.selesse.processes.ProcessRunner;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetGameIdFromGameOverlay {
    private static final List<String> COMMAND =
            Lists.newArrayList("wmic", "process", "where", "name='GameOverlayUI.exe'", "get", "CommandLine");

    public static long get() {
        String commandLineOutput = new ProcessRunner(COMMAND).runAndGetOutput();
        List<String> lines = Splitter.on("\n").splitToList(commandLineOutput);
        try {
            String arguments = lines.get(2);
            Pattern regex = Pattern.compile("-gameid (\\d+) ");
            Matcher matcher = regex.matcher(arguments);
            //noinspection ResultOfMethodCallIgnored
            matcher.find();
            String gameId = matcher.group(1);
            return Long.parseLong(gameId);
        } catch (RuntimeException e) {
            return 0;
        }
    }
}
