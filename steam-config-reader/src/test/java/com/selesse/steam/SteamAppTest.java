package com.selesse.steam;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.registry.SteamRegistry;
import com.selesse.steam.registry.implementation.RegistryObject;
import java.util.List;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public class SteamAppTest {
    @Test
    public void getSupportedOperatingSystemsDefaultsToWindowsWhenOslistIsMissing() {
        SteamApp steamApp = appWithOslist(null);

        assertThat(steamApp.getSupportedOperatingSystems()).containsExactly(OperatingSystems.OperatingSystem.WINDOWS);
    }

    @Test
    public void getSupportedOperatingSystemsParsesKnownPlatforms() {
        SteamApp steamApp = appWithOslist("windows,macos,linux");

        assertThat(steamApp.getSupportedOperatingSystems())
                .containsExactlyInAnyOrder(
                        OperatingSystems.OperatingSystem.WINDOWS,
                        OperatingSystems.OperatingSystem.MAC,
                        OperatingSystems.OperatingSystem.LINUX);
    }

    @Test
    public void getSupportedOperatingSystemsIgnoresPlatformsOutsideWindowsMacLinux() {
        // Some VR titles list a companion "android" platform alongside their desktop platforms.
        SteamApp steamApp = appWithOslist("windows,android");

        assertThat(steamApp.getSupportedOperatingSystems()).containsExactly(OperatingSystems.OperatingSystem.WINDOWS);
    }

    @Test
    public void aSinglePlatformDepotSaysWhichBuildIsInstalled() {
        assertThat(appWithDepots(List.of("11")).getInstalledBuild()).isEqualTo(SteamApp.InstalledBuild.WINDOWS);
        assertThat(appWithDepots(List.of("12")).getInstalledBuild()).isEqualTo(SteamApp.InstalledBuild.LINUX);
    }

    @Test
    public void aDepotServingSeveralPlatformsSaysNothing() {
        // "windows,linux" is one depot shared by both builds - it is not evidence that either is
        // the one running, so it must not be read as a Linux (or Windows) install.
        assertThat(appWithDepots(List.of("13")).getInstalledBuild()).isEqualTo(SteamApp.InstalledBuild.UNKNOWN);
    }

    @Test
    public void anUntaggedDepotSaysNothing() {
        // Depots with no oslist are platform-agnostic content, installed alongside every build.
        assertThat(appWithDepots(List.of("14")).getInstalledBuild()).isEqualTo(SteamApp.InstalledBuild.UNKNOWN);
    }

    @Test
    public void aPlatformSpecificDepotOutvotesTheSharedOnesInstalledBesideIt() {
        // The real shape: one tagged depot plus shared content depots, in either order.
        assertThat(appWithDepots(List.of("14", "12", "13")).getInstalledBuild())
                .isEqualTo(SteamApp.InstalledBuild.LINUX);
        assertThat(appWithDepots(List.of("13", "14", "11")).getInstalledBuild())
                .isEqualTo(SteamApp.InstalledBuild.WINDOWS);
    }

    @Test
    public void aGameThatIsNotInstalledHereHasNoDepotsToReadFrom() {
        assertThat(appWithDepots(List.of()).getInstalledBuild()).isEqualTo(SteamApp.InstalledBuild.UNKNOWN);
    }

    // 11 is windows-only, 12 linux-only, 13 shared between both, 14 untagged.
    private SteamApp appWithDepots(List<String> installedDepotIds) {
        RegistryObject registryObject = TestVdf.parseWithoutCollapse("""
                "common"
                {
                  "gameid" "4242"
                  "name" "Test Game"
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
                  "14"
                  {
                    "config"
                    {
                    }
                  }
                }
                """);
        SteamRegistry steamRegistry = Mockito.mock(SteamRegistry.class);
        Mockito.when(steamRegistry.getInstalledDepotIds(ArgumentMatchers.anyLong()))
                .thenReturn(installedDepotIds);
        return new SteamApp(registryObject, new SteamInstall(steamRegistry, TestSteamInstall.ACCOUNT_ID));
    }

    private SteamApp appWithOslist(String oslist) {
        RegistryObject registryObject =
                TestVdf.parse("""
                "common"
                {
                  "name" "Test Game"
                %s}
                """.formatted(oslist == null ? "" : "  \"oslist\" \"%s\"%n".formatted(oslist)));
        return new SteamApp(registryObject, TestSteamInstall.get());
    }
}
