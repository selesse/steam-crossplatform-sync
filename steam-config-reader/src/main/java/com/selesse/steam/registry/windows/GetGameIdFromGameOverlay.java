package com.selesse.steam.registry.windows;

import com.selesse.processes.ProcessRunner;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Windows can't give us this the way Mac/Linux do: ProcessHandle.info().arguments() reads the
// target process's PEB via ReadProcessMemory, which needs privileges we don't have for another
// user's process and is commonly empty even when we do. Get-CimInstance reads CommandLine through
// WMI instead, which doesn't hit that restriction. (wmic itself was removed in recent Windows 11
// releases, so we go through PowerShell's WMI cmdlet instead of shelling out to wmic directly.)
public class GetGameIdFromGameOverlay {
    private static final List<String> COMMAND = List.of(
            "powershell",
            "-NoProfile",
            "-Command",
            "(Get-CimInstance -ClassName Win32_Process -Filter 'Name=''GameOverlayUI64.exe''').CommandLine");
    private static final Pattern GAME_ID_PATTERN = Pattern.compile("-gameid (\\d+) ");

    public static long get() {
        try {
            String commandLineOutput = new ProcessRunner(COMMAND).runAndGetOutput();
            Matcher matcher = GAME_ID_PATTERN.matcher(commandLineOutput);
            if (matcher.find()) {
                return Long.parseLong(matcher.group(1));
            }
        } catch (RuntimeException e) {
            // PowerShell/WMI unavailable, or GameOverlayUI64.exe's command line didn't contain a game id.
        }
        return 0;
    }
}
