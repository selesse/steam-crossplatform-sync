package com.selesse.steam;

import com.google.common.base.Splitter;
import com.selesse.files.RuntimeExceptionFiles;
import com.selesse.os.Resources;
import com.selesse.steam.registry.RegistryPrettyPrint;
import com.selesse.steam.registry.implementation.RegistryParser;
import com.selesse.steam.registry.implementation.RegistryStore;
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
    UNRAILED(1016920),
    ORI_AND_THE_WILL_OF_THE_WISPS(1057090),
    WILDERMYTH(763890),
    PATH_OF_EXILE(238960),
    JACKBOX(331670),
    ;

    private final int gameId;

    TestGames(int gameId) {
        this.gameId = gameId;
    }

    public long getGameId() {
        return gameId;
    }

    public RegistryStore getGameRegistryStore() {
        return RegistryParser.parseWithoutRegistryCollapse(registryFileContents());
    }

    public List<String> registryFileContents() {
        if (Resources.exists(getGameId() + ".vdf")) {
            return registryFileContentsFromFile();
        } else {
            TestAppCache.setup();
            RegistryStore registryStore = SteamAppLoader.load(getGameId()).getRegistryStore();
            return Splitter.on("\n").splitToList(RegistryPrettyPrint.prettyPrint(registryStore));
        }
    }

    public List<String> registryFileContentsFromFile() {
        Path fakeFilePath = Resources.getResource(getGameId() + ".vdf");
        return RuntimeExceptionFiles.readAllLines(fakeFilePath);
    }
}
