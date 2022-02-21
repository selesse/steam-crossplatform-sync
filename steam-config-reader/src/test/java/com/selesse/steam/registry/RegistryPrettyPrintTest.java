package com.selesse.steam.registry;

import com.selesse.os.Resources;
import com.selesse.steam.AppCacheReader;
import com.selesse.steam.SteamAppLoader;
import com.selesse.steam.TestGames;
import com.selesse.steam.registry.implementation.RegistryParser;
import com.selesse.steam.registry.implementation.RegistryStore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class RegistryPrettyPrintTest {
    @Test
    public void canPrettyPrintInscryption() {
        String prettyPrint = RegistryPrettyPrint.prettyPrint(TestGames.INSCRYPTION.getGameRegistryStore());

        assertThat(prettyPrint).isEqualTo(String.join("\n", TestGames.INSCRYPTION.registryFileContents()) + "\n");
    }

    @Test
    public void canPrettyPrintInscryptionBasedOnAppCache() throws IOException {
        SteamAppLoader.primeAppCache(new AppCacheReader().load(Resources.getResource("appinfo.vdf")));
        RegistryStore registryStore = SteamAppLoader.load(TestGames.INSCRYPTION.getGameId()).getRegistryStore();
        String prettyPrint = RegistryPrettyPrint.prettyPrint(registryStore);

        String expected = String.join("\n", TestGames.INSCRYPTION.registryFileContents()) + "\n";

        assertThat(prettyPrint).isEqualTo(expected);
    }

    @Test
    public void prettyPrintingPrettyPrintResultsInTheSame() {
        String prettyPrint = RegistryPrettyPrint.prettyPrint(TestGames.HOLLOW_KNIGHT.getGameRegistryStore());

        List<String> prettyPrintedLines = Stream.of(prettyPrint.split("\n")).collect(Collectors.toList());
        RegistryStore registryStore = RegistryParser.parseWithoutRegistryCollapse(prettyPrintedLines);
        String prettyPrint2 = RegistryPrettyPrint.prettyPrint(registryStore);

        assertThat(prettyPrint).isEqualTo(prettyPrint2);
    }

    @Test
    public void prettyPrinting_pathOfExile_handlesKeysWithSlashesInThem() {
        SteamAppLoader.primeAppCache(new AppCacheReader().load(Resources.getResource("appinfo.vdf")));
        RegistryStore registryStore = SteamAppLoader.load(TestGames.PATH_OF_EXILE.getGameId()).getRegistryStore();
        String prettyPrintFromAppInfo = RegistryPrettyPrint.prettyPrint(registryStore);

        String expected = String.join("\n", TestGames.PATH_OF_EXILE.registryFileContentsFromFile()) + "\n";

        assertThat(prettyPrintFromAppInfo).isEqualTo(expected);
    }
}