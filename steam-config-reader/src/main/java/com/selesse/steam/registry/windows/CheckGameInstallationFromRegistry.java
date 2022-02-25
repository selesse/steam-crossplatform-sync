package com.selesse.steam.registry.windows;

import com.selesse.processes.ProcessRunner;
import java.util.List;
import java.util.function.Function;

public class CheckGameInstallationFromRegistry {
    private static final Function<Long, List<String>> GAME_QUERY =
            (gameId) -> List.of("reg", "query", "HKEY_CURRENT_USER\\Software\\Valve\\Steam\\apps\\" + gameId);

    public static boolean isInstalled(long gameId) {
        String individualGameRegistryOutput = new ProcessRunner(GAME_QUERY.apply(gameId)).runAndGetOutput();
        return RegistryDwordParser.getValueFromOutput(individualGameRegistryOutput, "Installed")
                .map(x -> x == 1)
                .orElse(false);
    }
}
