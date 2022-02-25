package com.selesse.steam.steamcmd;

import com.selesse.steam.registry.GameRegistryLoader;
import com.selesse.steam.registry.RegistryNotFoundException;
import com.selesse.steam.registry.implementation.RegistryParser;
import com.selesse.steam.registry.implementation.RegistryStore;
import java.util.List;

public class SteamCmdRegistryLoader implements GameRegistryLoader {
    @Override
    public RegistryStore load(long gameId) throws RegistryNotFoundException {
        PrintAppInfoExecutor printAppInfoExecutor = new PrintAppInfoExecutor();
        List<String> registryLines = printAppInfoExecutor.runPrintAppInfoProcess(gameId);
        if (registryLinesAreEmpty(gameId, registryLines)) {
            throw new RegistryNotFoundException();
        }
        return RegistryParser.parse(registryLines);
    }

    private boolean registryLinesAreEmpty(long gameId, List<String> registryLines) {
        return registryLines.size() == 3 && String.join(" ", registryLines).equals(getEmptyRegistryString(gameId));
    }

    private String getEmptyRegistryString(long gameId) {
        return String.format("\"%s\" { }", gameId);
    }
}
