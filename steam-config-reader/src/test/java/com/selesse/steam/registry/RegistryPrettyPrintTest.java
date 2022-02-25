package com.selesse.steam.registry;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.steam.SteamAppLoader;
import com.selesse.steam.TestAppCache;
import com.selesse.steam.TestGames;
import com.selesse.steam.registry.implementation.RegistryParser;
import com.selesse.steam.registry.implementation.RegistryStore;
import java.util.List;
import java.util.stream.Stream;
import org.junit.Test;

public class RegistryPrettyPrintTest {
    @Test
    public void canPrettyPrintInscryption() {
        String prettyPrint = RegistryPrettyPrint.prettyPrint(TestGames.INSCRYPTION.getGameRegistryStore());

        assertThat(prettyPrint).isEqualTo(String.join("\n", TestGames.INSCRYPTION.registryFileContents()) + "\n");
    }

    @Test
    public void canPrettyPrintInscryptionBasedOnAppCache() {
        TestAppCache.setup();
        RegistryStore registryStore =
                SteamAppLoader.load(TestGames.INSCRYPTION.getGameId()).getRegistryStore();
        String prettyPrint = RegistryPrettyPrint.prettyPrint(registryStore);

        String expected = String.join("\n", TestGames.INSCRYPTION.registryFileContents()) + "\n";

        assertThat(prettyPrint).isEqualTo(expected);
    }

    @Test
    public void prettyPrintingPrettyPrintResultsInTheSame() {
        String prettyPrint = RegistryPrettyPrint.prettyPrint(TestGames.HOLLOW_KNIGHT.getGameRegistryStore());

        List<String> prettyPrintedLines = Stream.of(prettyPrint.split("\n")).toList();
        RegistryStore registryStore = RegistryParser.parseWithoutRegistryCollapse(prettyPrintedLines);
        String prettyPrint2 = RegistryPrettyPrint.prettyPrint(registryStore);

        assertThat(prettyPrint).isEqualTo(prettyPrint2);
    }

    @Test
    public void prettyPrinting_pathOfExile_handlesKeysWithSlashesInThem() {
        TestAppCache.setup();
        RegistryStore registryStore =
                SteamAppLoader.load(TestGames.PATH_OF_EXILE.getGameId()).getRegistryStore();
        String prettyPrintFromAppInfo = RegistryPrettyPrint.prettyPrint(registryStore);

        String expected = String.join("\n", TestGames.PATH_OF_EXILE.registryFileContentsFromFile()) + "\n";

        assertThat(prettyPrintFromAppInfo).isEqualTo(expected);
    }
}
