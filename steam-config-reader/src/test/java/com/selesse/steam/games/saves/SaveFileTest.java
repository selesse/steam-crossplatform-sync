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
    public void aGameWithNoLinuxBuildResolvesUnderTheProtonPrefix() {
        SteamApp steamApp = windowsOnlyGame(installWithNoDepots());

        List<UserFileSystemPath> paths = new SaveFile(steamApp).savePathsFor(OperatingSystem.LINUX);

        assertThat(paths).hasSize(1);
        assertThat(paths.get(0).getSymbolPath())
                .isEqualTo(SteamInstallationPaths.getProtonPrefixUserProfileRoot(TEST_APP_ID)
                        + "/AppData/LocalLow/Test Game/save/*.sav");
    }

    @Test
    public void anExplicitLinuxRootOverrideBeatsTheWeakerSignalsWhenTheDepotsAbstain() {
        SteamApp steamApp = realFixtureSteamApp(TestGames.WARGROOVE, installWithNoDepots());
        String xdgDataHome = OsAgnosticPaths.of(System.getenv().getOrDefault("XDG_DATA_HOME", "~/.local/share"));

        List<UserFileSystemPath> paths = new SaveFile(steamApp).savePathsFor(OperatingSystem.LINUX);

        assertThat(paths).hasSize(1);
        assertThat(paths.get(0).getSymbolPath()).isEqualTo(xdgDataHome + "/Chucklefish/Wargroove/save/*");
    }

    @Test
    public void explicitLinuxRootOverrideUsingXdgConfigHomeResolvesCorrectly() {
        // Regression test: "LinuxXdgConfigHome" used to pass through SteamPathConverter unconverted.
        SteamApp steamApp = gameWithLinuxXdgConfigHomeOverride(installWithNoDepots());
        String xdgConfigHome = OsAgnosticPaths.of(System.getenv().getOrDefault("XDG_CONFIG_HOME", "~/.config"));

        List<UserFileSystemPath> paths = new SaveFile(steamApp).savePathsFor(OperatingSystem.LINUX);

        assertThat(paths).hasSize(1);
        assertThat(paths.get(0).getSymbolPath()).isEqualTo(xdgConfigHome + "/unity3d/TestCo/Test Game/Saves/*");
    }

    @Test
    public void gameInstallRootedSaveIsUnaffectedByProton() {
        // A gameinstall-rooted save is the same directory on disk under either build.
        SteamApp steamApp = realFixtureSteamApp(TestGames.INSCRYPTION, installWithNoDepots());

        List<UserFileSystemPath> paths = new SaveFile(steamApp).savePathsFor(OperatingSystem.LINUX);

        assertThat(paths).hasSize(1);
        assertThat(paths.get(0).getRoot()).doesNotContain("compatdata");
    }

    // Hollow Knight's shape: a Windows depot installed alongside a declared Linux save location. The
    // override describes the native build, which is not the one running.
    @Test
    public void anInstalledWindowsDepotBeatsAnExplicitLinuxOverride() {
        SteamApp steamApp = gameWithPerOsDepots(installWithDepots(List.of("11")), true, "windows,linux");

        List<UserFileSystemPath> paths = new SaveFile(steamApp).savePathsFor(OperatingSystem.LINUX);

        assertThat(paths).hasSize(1);
        assertThat(paths.get(0).getSymbolPath())
                .isEqualTo(SteamInstallationPaths.getProtonPrefixUserProfileRoot(TEST_APP_ID)
                        + "/AppData/LocalLow/TestCo/Test Game/Saves/*");
    }

    @Test
    public void anInstalledLinuxDepotResolvesToTheNativeLocation() {
        SteamApp steamApp = gameWithPerOsDepots(installWithDepots(List.of("12")), true, "windows,linux");
        String xdgConfigHome = OsAgnosticPaths.of(System.getenv().getOrDefault("XDG_CONFIG_HOME", "~/.config"));

        List<UserFileSystemPath> paths = new SaveFile(steamApp).savePathsFor(OperatingSystem.LINUX);

        assertThat(paths).hasSize(1);
        assertThat(paths.get(0).getSymbolPath()).isEqualTo(xdgConfigHome + "/unity3d/TestCo/Test Game/Saves/*");
    }

    // Depot 13 serves both builds, so it says nothing about which is running and oslist decides.
    @Test
    public void aDepotServingBothPlatformsLeavesTheDecisionToOslist() {
        SteamApp withoutLinuxBuild = gameWithPerOsDepots(installWithDepots(List.of("13")), false, "windows");
        SteamApp withLinuxBuild = gameWithPerOsDepots(installWithDepots(List.of("13")), false, "windows,linux");

        assertThat(new SaveFile(withoutLinuxBuild)
                        .savePathsFor(OperatingSystem.LINUX)
                        .get(0)
                        .getSymbolPath())
                .isEqualTo(SteamInstallationPaths.getProtonPrefixUserProfileRoot(TEST_APP_ID)
                        + "/AppData/LocalLow/TestCo/Test Game/Saves/*");
        assertThat(new SaveFile(withLinuxBuild)
                        .savePathsFor(OperatingSystem.LINUX)
                        .get(0)
                        .getSymbolPath())
                .doesNotContain("compatdata");
    }

    @Test
    public void anUninstalledGameFallsBackToOslist() {
        SteamApp steamApp = windowsOnlyGame(installWithNoDepots());

        List<UserFileSystemPath> paths = new SaveFile(steamApp).savePathsFor(OperatingSystem.LINUX);

        assertThat(paths).hasSize(1);
        assertThat(paths.get(0).getSymbolPath())
                .isEqualTo(SteamInstallationPaths.getProtonPrefixUserProfileRoot(TEST_APP_ID)
                        + "/AppData/LocalLow/Test Game/save/*.sav");
    }

    private SteamInstall installWithNoDepots() {
        return installWithDepots(List.of());
    }

    private SteamInstall installWithDepots(List<String> installedDepotIds) {
        SteamRegistry steamRegistry = Mockito.mock(SteamRegistry.class);
        when(steamRegistry.getInstalledDepotIds(anyLong())).thenReturn(installedDepotIds);
        return new SteamInstall(steamRegistry, TestSteamInstall.ACCOUNT_ID);
    }

    // Depot 11 is windows-only, 12 is linux-only, 13 serves both. Which one the manifest says is
    // installed is what each test varies.
    private SteamApp gameWithPerOsDepots(SteamInstall install, boolean withLinuxOverride, String oslist) {
        String vdf = """
                "common"
                {
                  "gameid" "%d"
                  "name" "Test Game"
                  "oslist" "%s"
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
                """.formatted(TEST_APP_ID, oslist, withLinuxOverride ? LINUX_XDG_CONFIG_HOME_OVERRIDE : "");
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
