package com.selesse.steam.steamcmd.remote;

import com.selesse.steam.registry.GameRegistryLoader;
import com.selesse.steam.registry.RegistryNotFoundException;
import com.selesse.steam.registry.implementation.RegistryParser;
import com.selesse.steam.registry.implementation.RegistryStore;
import java.util.List;

public class RemoteGameRegistryLoader implements GameRegistryLoader {
    private final String remoteAppInfoUrl;

    public RemoteGameRegistryLoader(String remoteAppInfoUrl) {
        this.remoteAppInfoUrl = remoteAppInfoUrl;
    }

    @Override
    public RegistryStore load(long gameId) throws RegistryNotFoundException {
        List<String> registryLines = new RemoteAppInfoFetcher(remoteAppInfoUrl).fetch(gameId);
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
