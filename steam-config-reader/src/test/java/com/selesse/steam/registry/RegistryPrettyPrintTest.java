package com.selesse.steam.registry;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.steam.SteamAppLoader;
import com.selesse.steam.TestAppCache;
import com.selesse.steam.TestGames;
import com.selesse.steam.TestSteamInstall;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryParser;
import java.util.List;
import java.util.stream.Stream;
import org.junit.Test;

public class RegistryPrettyPrintTest {
    @Test
    public void canPrettyPrintInscryption() {
        String prettyPrint = RegistryPrettyPrint.prettyPrint(TestGames.INSCRYPTION.getGameRegistryObject());

        assertThat(prettyPrint).isEqualTo(String.join("\n", TestGames.INSCRYPTION.registryFileContents()) + "\n");
    }

    @Test
    public void canPrettyPrintInscryptionBasedOnAppCache() {
        RegistryObject registryObject = new SteamAppLoader(TestSteamInstall.get())
                .load(TestAppCache.PATH, TestGames.INSCRYPTION.getGameId())
                .getRegistryObject();
        String prettyPrint = RegistryPrettyPrint.prettyPrint(registryObject);

        String expected = String.join("\n", TestGames.INSCRYPTION.registryFileContents()) + "\n";

        assertThat(prettyPrint).isEqualTo(expected);
    }

    @Test
    public void prettyPrintingPrettyPrintResultsInTheSame() {
        String prettyPrint = RegistryPrettyPrint.prettyPrint(TestGames.HOLLOW_KNIGHT.getGameRegistryObject());

        List<String> prettyPrintedLines = Stream.of(prettyPrint.split("\n")).toList();
        RegistryObject registryObject = RegistryParser.parseWithoutRegistryCollapse(prettyPrintedLines);
        String prettyPrint2 = RegistryPrettyPrint.prettyPrint(registryObject);

        assertThat(prettyPrint).isEqualTo(prettyPrint2);
    }

    @Test
    public void prettyPrinting_pathOfExile_handlesKeysWithSlashesInThem() {
        RegistryObject registryObject = new SteamAppLoader(TestSteamInstall.get())
                .load(TestAppCache.PATH, TestGames.PATH_OF_EXILE.getGameId())
                .getRegistryObject();
        String prettyPrintFromAppInfo = RegistryPrettyPrint.prettyPrint(registryObject);

        String expected = String.join("\n", TestGames.PATH_OF_EXILE.registryFileContentsFromFile()) + "\n";

        assertThat(prettyPrintFromAppInfo).isEqualTo(expected);
    }
}
