package com.selesse.steam.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.selesse.os.Resources;
import com.selesse.steam.registry.implementation.RegistryObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.Test;
import org.mockito.Mockito;

public class SteamRegistryTest {
    @Test
    public void readVdfParsesAFile() {
        Path path = Resources.getResource("loginusers.vdf");

        RegistryObject registryObject = new SteamRegistry().readVdf(path);

        assertThat(registryObject.pathExists("users/76561197960287930/AccountName"))
                .isTrue();
        assertThat(registryObject
                        .getObjectValueAsString("users/76561197960287930/AccountName")
                        .getValue())
                .isEqualTo("rabscuttle");
    }

    @Test
    public void readVdfIfPresentReturnsEmptyWhenFileIsMissing() {
        Path path = Path.of("does/not/exist.vdf");

        Optional<RegistryObject> result = new SteamRegistry().readVdfIfPresent(path);

        assertThat(result).isEmpty();
    }

    @Test
    public void readVdfIfPresentParsesTheFileWhenItExists() {
        Path path = Resources.getResource("loginusers.vdf");

        Optional<RegistryObject> result = new SteamRegistry().readVdfIfPresent(path);

        assertThat(result).isPresent();
        assertThat(result.get().pathExists("users")).isTrue();
    }

    @Test
    public void hasActiveProtonPrefixIsFalseWhenCompatDataIsMissing() throws IOException {
        Path steamApps = Files.createTempDirectory("steamapps");
        SteamRegistry steamRegistry = spyOnSteamApps(steamApps);

        assertThat(steamRegistry.hasActiveProtonPrefix(646570L)).isFalse();
    }

    @Test
    public void hasActiveProtonPrefixIsFalseWhenDriveCIsEmpty() throws IOException {
        Path steamApps = Files.createTempDirectory("steamapps");
        Files.createDirectories(steamApps.resolve("compatdata/646570/pfx/drive_c"));
        SteamRegistry steamRegistry = spyOnSteamApps(steamApps);

        assertThat(steamRegistry.hasActiveProtonPrefix(646570L)).isFalse();
    }

    @Test
    public void hasActiveProtonPrefixIsTrueWhenDriveCHasContent() throws IOException {
        Path steamApps = Files.createTempDirectory("steamapps");
        Path driveC = steamApps.resolve("compatdata/646570/pfx/drive_c");
        Files.createDirectories(driveC.resolve("users/steamuser"));
        SteamRegistry steamRegistry = spyOnSteamApps(steamApps);

        assertThat(steamRegistry.hasActiveProtonPrefix(646570L)).isTrue();
    }

    private SteamRegistry spyOnSteamApps(Path steamApps) {
        SteamRegistry steamRegistry = Mockito.spy(new SteamRegistry());
        doReturn(steamApps).when(steamRegistry).getSteamAppsPath();
        return steamRegistry;
    }
}
