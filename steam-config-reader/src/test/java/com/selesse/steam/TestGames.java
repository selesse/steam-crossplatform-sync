package com.selesse.steam;

import com.selesse.os.Resources;
import com.selesse.steam.registry.implementation.RegistryParser;
import com.selesse.steam.registry.implementation.RegistryStore;
import com.selesse.steam.games.UserFileSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public enum TestGames {
    HOLLOW_KNIGHT(367520),
    OXYGEN_NOT_INCLUDED(457140),
    WARGROOVE(607050),
    SLAY_THE_SPIRE(646570),
    LEGEND_OF_GRIMROCK(207170),
    TORCHLIGHT_II(200710),
    INSCRYPTION(1092790),
    ;

    private final int gameId;

    TestGames(int gameId) {
        this.gameId = gameId;
    }

    public long getGameId() {
        return gameId;
    }

    public UserFileSystem getUserFileSystem() {
        return new UserFileSystem(RegistryParser.parseOmittingFirstLevel(registryFileContents()));
    }

    public RegistryStore getGameRegistryStore() {
        return RegistryParser.parse(registryFileContents());
    }

    public List<String> registryFileContents() {
        Path fakeFilePath = Resources.getResource(getGameId() + ".vdf");
        try {
            return Files.readAllLines(fakeFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
