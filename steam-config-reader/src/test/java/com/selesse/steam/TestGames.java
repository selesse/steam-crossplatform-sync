package com.selesse.steam;

import com.google.common.io.Resources;
import com.selesse.steam.registry.implementation.RegistryParser;
import com.selesse.steam.registry.implementation.RegistryStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public enum TestGames {
    HOLLOW_KNIGHT(367520),
    OXYGEN_NOT_INCLUDED(457140),
    WARGROOVE(607050),
    ;

    private final int gameId;

    TestGames(int gameId) {
        this.gameId = gameId;
    }

    public RegistryStore getUserFileSystemStore() {
        return new RegistryStore(getRegistryStore().getObjectValueAsObject("ufs"));
    }

    private RegistryStore getRegistryStore() {
        return RegistryParser.parseOmittingFirstLevel(registryFileContents());
    }

    private List<String> registryFileContents() {
        Path fakeFilePath = Path.of(Resources.getResource(gameId + ".vdf").getPath());
        try {
            return Files.readAllLines(fakeFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
