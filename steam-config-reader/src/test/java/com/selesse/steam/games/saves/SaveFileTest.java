package com.selesse.steam.games.saves;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.selesse.files.OsAgnosticPaths;
import com.selesse.os.OperatingSystems.OperatingSystem;
import com.selesse.steam.SteamApp;
import com.selesse.steam.SteamInstall;
import com.selesse.steam.TestGames;
import com.selesse.steam.TestSteamInstall;
import com.selesse.steam.games.SteamInstallationPaths;
import com.selesse.steam.games.UserFileSystemPath;
import com.selesse.steam.registry.SteamRegistry;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryParser;
import java.util.List;
import org.junit.Test;
import org.mockito.Mockito;

public class SaveFileTest {
    private static final long TEST_APP_ID = 9999991L;

    @Test
    public void windowsOnlyGameWithProtonActiveResolvesUnderTheProtonPrefix() {
        SteamApp steamApp = windowsOnlyGame(protonInstall(true));

        List<UserFileSystemPath> paths = new SaveFile(steamApp).savePathsFor(OperatingSystem.LINUX);

        assertThat(paths).hasSize(1);
        assertThat(paths.get(0).getSymbolPath())
                .isEqualTo(SteamInstallationPaths.getProtonPrefixUserProfileRoot(TEST_APP_ID)
                        + "/AppData/LocalLow/Test Game/save/*.sav");
    }

    @Test
    public void windowsOnlyGameNeverLaunchedStillResolvesUnderTheProtonPrefix() {
        // No native Linux depot exists at all, so Proton is the only way this game can ever run here,
        // regardless of whether it's been launched yet (i.e. before compatdata/pfx even exists).
        SteamApp steamApp = windowsOnlyGame(protonInstall(false));

        List<UserFileSystemPath> paths = new SaveFile(steamApp).savePathsFor(OperatingSystem.LINUX);

        assertThat(paths).hasSize(1);
        assertThat(paths.get(0).getSymbolPath())
                .isEqualTo(SteamInstallationPaths.getProtonPrefixUserProfileRoot(TEST_APP_ID)
                        + "/AppData/LocalLow/Test Game/save/*.sav");
    }

    @Test
    public void explicitLinuxRootOverrideWinsOverProton() {
        // Wargroove has no native Linux depot but declares an explicit Linux rootoverride
        // (useinstead: LinuxXdgDataHome). Even with Proton "active", that explicit override must win.
        SteamApp steamApp = realFixtureSteamApp(TestGames.WARGROOVE, protonInstall(true));
        String xdgDataHome = OsAgnosticPaths.of(System.getenv().getOrDefault("XDG_DATA_HOME", "~/.local/share"));

        List<UserFileSystemPath> paths = new SaveFile(steamApp).savePathsFor(OperatingSystem.LINUX);

        assertThat(paths).hasSize(1);
        assertThat(paths.get(0).getSymbolPath()).isEqualTo(xdgDataHome + "/Chucklefish/Wargroove/save/*");
    }

    @Test
    public void explicitLinuxRootOverrideUsingXdgConfigHomeResolvesCorrectly() {
        // Regression test for the real Rogue Legacy 2 bug: an explicit rootoverride using
        // "LinuxXdgConfigHome" used to pass through SteamPathConverter unconverted. Also proves
        // this explicit override wins over Proton, exactly like the Wargroove case above.
        SteamApp steamApp = gameWithLinuxXdgConfigHomeOverride(protonInstall(true));
        String xdgConfigHome = OsAgnosticPaths.of(System.getenv().getOrDefault("XDG_CONFIG_HOME", "~/.config"));

        List<UserFileSystemPath> paths = new SaveFile(steamApp).savePathsFor(OperatingSystem.LINUX);

        assertThat(paths).hasSize(1);
        assertThat(paths.get(0).getSymbolPath()).isEqualTo(xdgConfigHome + "/unity3d/TestCo/Test Game/Saves/*");
    }

    @Test
    public void gameInstallRootedSaveIsUnaffectedByProton() {
        // Inscryption's saves live inside the install directory itself, which is the same real
        // directory on disk whether Steam installed the Windows or Linux depot there.
        SteamApp steamApp = realFixtureSteamApp(TestGames.INSCRYPTION, protonInstall(true));

        List<UserFileSystemPath> paths = new SaveFile(steamApp).savePathsFor(OperatingSystem.LINUX);

        assertThat(paths).hasSize(1);
        assertThat(paths.get(0).getRoot()).doesNotContain("compatdata");
    }

    private SteamInstall protonInstall(boolean active) {
        SteamRegistry steamRegistry = Mockito.mock(SteamRegistry.class);
        when(steamRegistry.hasActiveProtonPrefix(anyLong())).thenReturn(active);
        return new SteamInstall(steamRegistry, TestSteamInstall.ACCOUNT_ID);
    }

    private SteamApp windowsOnlyGame(SteamInstall install) {
        List<String> lines = List.of(
                "\"common\"",
                "{",
                "\t\"gameid\"\t\"" + TEST_APP_ID + "\"",
                "\t\"name\"\t\"Test Game\"",
                "\t\"oslist\"\t\"windows\"",
                "}",
                "\"ufs\"",
                "{",
                "\t\"savefiles\"",
                "\t{",
                "\t\t\"0\"",
                "\t\t{",
                "\t\t\t\"root\"\t\"WinAppDataLocalLow\"",
                "\t\t\t\"path\"\t\"Test Game/save\"",
                "\t\t\t\"pattern\"\t\"*.sav\"",
                "\t\t}",
                "\t}",
                "}");
        return new SteamApp(RegistryParser.parseWithoutRegistryCollapse(lines), install);
    }

    private SteamApp gameWithLinuxXdgConfigHomeOverride(SteamInstall install) {
        List<String> lines = List.of(
                "\"common\"",
                "{",
                "\t\"gameid\"\t\"" + TEST_APP_ID + "\"",
                "\t\"name\"\t\"Test Game\"",
                "\t\"oslist\"\t\"windows,linux\"",
                "}",
                "\"ufs\"",
                "{",
                "\t\"savefiles\"",
                "\t{",
                "\t\t\"0\"",
                "\t\t{",
                "\t\t\t\"root\"\t\"WinAppDataLocalLow\"",
                "\t\t\t\"path\"\t\"TestCo/Test Game/Saves\"",
                "\t\t\t\"pattern\"\t\"*\"",
                "\t\t}",
                "\t}",
                "\t\"rootoverrides\"",
                "\t{",
                "\t\t\"0\"",
                "\t\t{",
                "\t\t\t\"root\"\t\"WinAppDataLocalLow\"",
                "\t\t\t\"os\"\t\"Linux\"",
                "\t\t\t\"oscompare\"\t\"=\"",
                "\t\t\t\"useinstead\"\t\"LinuxXdgConfigHome\"",
                "\t\t\t\"addpath\"\t\"unity3d\"",
                "\t\t}",
                "\t}",
                "}");
        RegistryObject registryObject = RegistryParser.parseWithoutRegistryCollapse(lines);
        return new SteamApp(registryObject, install);
    }

    private SteamApp realFixtureSteamApp(TestGames testGame, SteamInstall install) {
        RegistryObject registryObject = RegistryParser.parse(testGame.registryFileContentsFromFile());
        return new SteamApp(registryObject, install);
    }
}
