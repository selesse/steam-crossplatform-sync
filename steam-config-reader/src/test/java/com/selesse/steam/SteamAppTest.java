package com.selesse.steam;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.registry.implementation.RegistryParser;
import com.selesse.steam.registry.implementation.RegistryStore;
import java.util.List;
import org.junit.Test;

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

    private SteamApp appWithOslist(String oslist) {
        List<String> lines = oslist == null
                ? List.of("\"common\"", "{", "\t\"name\"\t\"Test Game\"", "}")
                : List.of("\"common\"", "{", "\t\"name\"\t\"Test Game\"", "\t\"oslist\"\t\"" + oslist + "\"", "}");
        RegistryStore registryStore = RegistryParser.parse(lines);
        return new SteamApp(registryStore);
    }
}
