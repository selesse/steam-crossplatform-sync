package com.selesse.steam.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.selesse.os.Resources;
import com.selesse.steam.registry.implementation.RegistryObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
    public void findProtonPrefixIsEmptyWhenSteamHasNotCreatedOne() throws IOException {
        SteamRegistry steamRegistry = spyOnLibraries(Files.createTempDirectory("steamapps"));

        assertThat(steamRegistry.findProtonPrefix(646570L)).isEmpty();
    }

    // Steam puts a prefix in whichever library it likes, so looking only in the primary one misses
    // prefixes for games installed on an SD card.
    @Test
    public void findProtonPrefixLooksInEveryLibrary() throws IOException {
        Path primary = Files.createTempDirectory("steamapps");
        Path secondary = Files.createTempDirectory("steamapps-sd");
        Path prefix = secondary.resolve("compatdata/646570/pfx");
        Files.createDirectories(prefix);
        SteamRegistry steamRegistry = spyOnLibraries(primary, secondary);

        assertThat(steamRegistry.findProtonPrefix(646570L)).contains(prefix);
    }

    private SteamRegistry spyOnLibraries(Path... steamApps) {
        SteamRegistry steamRegistry = Mockito.spy(new SteamRegistry());
        doReturn(List.of(steamApps)).when(steamRegistry).getLibrarySteamAppsPaths();
        return steamRegistry;
    }
}
