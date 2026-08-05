package com.selesse.steam;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.registry.SteamRegistry;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryParser;
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

    private SteamApp appWithDepots(List<String> installedDepotIds) {
        List<String> lines = List.of(
                "\"common\"",
                "{",
                "\t\"gameid\"\t\"4242\"",
                "\t\"name\"\t\"Test Game\"",
                "}",
                "\"depots\"",
                "{",
                "\t\"11\"",
                "\t{",
                "\t\t\"config\"",
                "\t\t{",
                "\t\t\t\"oslist\"\t\"windows\"",
                "\t\t}",
                "\t}",
                "\t\"12\"",
                "\t{",
                "\t\t\"config\"",
                "\t\t{",
                "\t\t\t\"oslist\"\t\"linux\"",
                "\t\t}",
                "\t}",
                "\t\"13\"",
                "\t{",
                "\t\t\"config\"",
                "\t\t{",
                "\t\t\t\"oslist\"\t\"windows,linux\"",
                "\t\t}",
                "\t}",
                "\t\"14\"",
                "\t{",
                "\t\t\"config\"",
                "\t\t{",
                "\t\t}",
                "\t}",
                "}");
        SteamRegistry steamRegistry = Mockito.mock(SteamRegistry.class);
        Mockito.when(steamRegistry.getInstalledDepotIds(ArgumentMatchers.anyLong()))
                .thenReturn(installedDepotIds);
        return new SteamApp(
                RegistryParser.parseWithoutRegistryCollapse(lines),
                new SteamInstall(steamRegistry, TestSteamInstall.ACCOUNT_ID));
    }

    private SteamApp appWithOslist(String oslist) {
        List<String> lines = oslist == null
                ? List.of("\"common\"", "{", "\t\"name\"\t\"Test Game\"", "}")
                : List.of("\"common\"", "{", "\t\"name\"\t\"Test Game\"", "\t\"oslist\"\t\"" + oslist + "\"", "}");
        RegistryObject registryObject = RegistryParser.parse(lines);
        return new SteamApp(registryObject, TestSteamInstall.get());
    }
}
