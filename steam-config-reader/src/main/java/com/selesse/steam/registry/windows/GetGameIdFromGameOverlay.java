package com.selesse.steam.registry.windows;

import com.selesse.processes.ProcessRunner;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Windows can't give us this the way Mac/Linux do: ProcessHandle.info().arguments() reads the
// target process's PEB via ReadProcessMemory, which needs privileges we don't have for another
// user's process and is commonly empty even when we do. Get-CimInstance reads CommandLine through
// WMI instead, which doesn't hit that restriction. (wmic itself was removed in recent Windows 11
// releases, so we go through PowerShell's WMI cmdlet instead of shelling out to wmic directly.)
public class GetGameIdFromGameOverlay {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetGameIdFromGameOverlay.class);

    private static final List<String> COMMAND = List.of(
            "powershell",
            "-NoProfile",
            "-Command",
            "(Get-CimInstance -ClassName Win32_Process -Filter 'Name=''GameOverlayUI64.exe''').CommandLine");
    private static final Pattern GAME_ID_PATTERN = Pattern.compile("-gameid (\\d+)");

    public static long get() {
        String commandLineOutput;
        try {
            commandLineOutput = new ProcessRunner(COMMAND).runAndGetOutput();
        } catch (RuntimeException e) {
            LOGGER.warn("Could not look up the game overlay's command line", e);
            return 0;
        }
        long gameId = parseGameId(commandLineOutput);
        if (gameId == 0) {
            // Only called once the overlay process exists, so a missing id is a lookup problem, not "no game".
            LOGGER.warn(
                    "Game overlay is running but no game id was found in its command line: '{}'",
                    commandLineOutput.strip());
        }
        return gameId;
    }

    static long parseGameId(String commandLineOutput) {
        Matcher matcher = GAME_ID_PATTERN.matcher(commandLineOutput);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : 0;
    }
}
