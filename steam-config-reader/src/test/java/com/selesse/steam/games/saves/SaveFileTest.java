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
import com.selesse.steam.TestVdf;
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

    // Rogue Legacy 2's real shape: the native Linux build keeps saves under XDG config home.
    private static final String LINUX_XDG_CONFIG_HOME_OVERRIDE = """
              "rootoverrides"
              {
                "0"
                {
                  "root" "WinAppDataLocalLow"
                  "os" "Linux"
                  "oscompare" "="
                  "useinstead" "LinuxXdgConfigHome"
                  "addpath" "unity3d"
                }
              }
            """;

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

    // Hollow Knight on a real Deck: the Windows depot is what Steam installed, so it runs under
    // Proton - but it also declares a Linux rootoverride. Honouring the override sent resolution to
    // ~/.config/unity3d/..., which held one stale 2022 file while the live saves sat in the prefix.
    @Test
    public void anInstalledWindowsDepotBeatsAnExplicitLinuxOverride() {
        SteamApp steamApp = gameWithPerOsDepots(installWithDepots(false, List.of("11")), true);

        List<UserFileSystemPath> paths = new SaveFile(steamApp).savePathsFor(OperatingSystem.LINUX);

        assertThat(paths).hasSize(1);
        assertThat(paths.get(0).getSymbolPath())
                .isEqualTo(SteamInstallationPaths.getProtonPrefixUserProfileRoot(TEST_APP_ID)
                        + "/AppData/LocalLow/TestCo/Test Game/Saves/*");
    }

    // Rogue Legacy 2: a native Linux depot is installed, but a compatdata prefix survives from an
    // earlier Proton run. The prefix must not win - and the depot settles it without relying on the
    // rootoverride to act as a veto.
    @Test
    public void anInstalledLinuxDepotIgnoresALeftoverProtonPrefix() {
        SteamApp steamApp = gameWithPerOsDepots(installWithDepots(true, List.of("12")), true);
        String xdgConfigHome = OsAgnosticPaths.of(System.getenv().getOrDefault("XDG_CONFIG_HOME", "~/.config"));

        List<UserFileSystemPath> paths = new SaveFile(steamApp).savePathsFor(OperatingSystem.LINUX);

        assertThat(paths).hasSize(1);
        assertThat(paths.get(0).getSymbolPath()).isEqualTo(xdgConfigHome + "/unity3d/TestCo/Test Game/Saves/*");
    }

    // Wargroove 2 installs one depot tagged "windows,linux" - shared content serving both builds,
    // which says nothing about what is running. The depot signal abstains and the older heuristics
    // decide: no explicit Linux override here, and a live prefix, so Proton. Reading that shared
    // depot as "linux installed" would wrongly send this to the native path instead.
    @Test
    public void aDepotServingBothPlatformsAbstainsAndLeavesTheOlderHeuristicsInCharge() {
        SteamApp steamApp = gameWithPerOsDepots(installWithDepots(true, List.of("13")), false);

        List<UserFileSystemPath> paths = new SaveFile(steamApp).savePathsFor(OperatingSystem.LINUX);

        assertThat(paths).hasSize(1);
        assertThat(paths.get(0).getSymbolPath())
                .isEqualTo(SteamInstallationPaths.getProtonPrefixUserProfileRoot(TEST_APP_ID)
                        + "/AppData/LocalLow/TestCo/Test Game/Saves/*");
    }

    // A game that isn't installed here has no manifest, so nothing to read depots from.
    @Test
    public void anUninstalledGameFallsBackToTheOlderHeuristics() {
        SteamApp steamApp = windowsOnlyGame(installWithDepots(false, List.of()));

        List<UserFileSystemPath> paths = new SaveFile(steamApp).savePathsFor(OperatingSystem.LINUX);

        assertThat(paths).hasSize(1);
        assertThat(paths.get(0).getSymbolPath())
                .isEqualTo(SteamInstallationPaths.getProtonPrefixUserProfileRoot(TEST_APP_ID)
                        + "/AppData/LocalLow/Test Game/save/*.sav");
    }

    private SteamInstall protonInstall(boolean active) {
        SteamRegistry steamRegistry = Mockito.mock(SteamRegistry.class);
        when(steamRegistry.hasActiveProtonPrefix(anyLong())).thenReturn(active);
        return new SteamInstall(steamRegistry, TestSteamInstall.ACCOUNT_ID);
    }

    private SteamInstall installWithDepots(boolean protonPrefixActive, List<String> installedDepotIds) {
        SteamRegistry steamRegistry = Mockito.mock(SteamRegistry.class);
        when(steamRegistry.hasActiveProtonPrefix(anyLong())).thenReturn(protonPrefixActive);
        when(steamRegistry.getInstalledDepotIds(anyLong())).thenReturn(installedDepotIds);
        return new SteamInstall(steamRegistry, TestSteamInstall.ACCOUNT_ID);
    }

    // Depot 11 is windows-only, 12 is linux-only, 13 serves both. Which one the manifest says is
    // installed is what each test varies.
    private SteamApp gameWithPerOsDepots(SteamInstall install, boolean withLinuxOverride) {
        String vdf = """
                "common"
                {
                  "gameid" "%d"
                  "name" "Test Game"
                  "oslist" "windows,linux"
                }
                "depots"
                {
                  "11"
                  {
                    "config"
                    {
                      "oslist" "windows"
                    }
                  }
                  "12"
                  {
                    "config"
                    {
                      "oslist" "linux"
                    }
                  }
                  "13"
                  {
                    "config"
                    {
                      "oslist" "windows,linux"
                    }
                  }
                }
                "ufs"
                {
                  "savefiles"
                  {
                    "0"
                    {
                      "root" "WinAppDataLocalLow"
                      "path" "TestCo/Test Game/Saves"
                      "pattern" "*"
                    }
                  }
                %s}
                """.formatted(TEST_APP_ID, withLinuxOverride ? LINUX_XDG_CONFIG_HOME_OVERRIDE : "");
        return new SteamApp(TestVdf.parse(vdf), install);
    }

    private SteamApp windowsOnlyGame(SteamInstall install) {
        String vdf = """
                "common"
                {
                  "gameid" "%d"
                  "name" "Test Game"
                  "oslist" "windows"
                }
                "ufs"
                {
                  "savefiles"
                  {
                    "0"
                    {
                      "root" "WinAppDataLocalLow"
                      "path" "Test Game/save"
                      "pattern" "*.sav"
                    }
                  }
                }
                """.formatted(TEST_APP_ID);
        return new SteamApp(TestVdf.parse(vdf), install);
    }

    private SteamApp gameWithLinuxXdgConfigHomeOverride(SteamInstall install) {
        String vdf = """
                "common"
                {
                  "gameid" "%d"
                  "name" "Test Game"
                  "oslist" "windows,linux"
                }
                "ufs"
                {
                  "savefiles"
                  {
                    "0"
                    {
                      "root" "WinAppDataLocalLow"
                      "path" "TestCo/Test Game/Saves"
                      "pattern" "*"
                    }
                  }
                %s}
                """.formatted(TEST_APP_ID, LINUX_XDG_CONFIG_HOME_OVERRIDE);
        return new SteamApp(TestVdf.parse(vdf), install);
    }

    private SteamApp realFixtureSteamApp(TestGames testGame, SteamInstall install) {
        RegistryObject registryObject = RegistryParser.parseAppCacheEntry(testGame.registryFileContentsFromFile());
        return new SteamApp(registryObject, install);
    }
}
