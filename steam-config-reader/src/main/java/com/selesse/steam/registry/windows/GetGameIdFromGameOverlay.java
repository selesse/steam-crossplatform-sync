package com.selesse.steam.registry.windows;

import com.selesse.processes.ProcessRunner;
import com.selesse.text.Splitter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Windows can't give us this the way Mac/Linux do: ProcessHandle.info().arguments() reads the
// target process's PEB via ReadProcessMemory, which needs privileges we don't have for another
// user's process and is commonly empty even when we do. wmic reads CommandLine through WMI
// instead, which doesn't hit that restriction.
public class GetGameIdFromGameOverlay {
    private static final List<String> COMMAND =
            List.of("wmic", "process", "where", "name='GameOverlayUI64.exe'", "get", "CommandLine");

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
