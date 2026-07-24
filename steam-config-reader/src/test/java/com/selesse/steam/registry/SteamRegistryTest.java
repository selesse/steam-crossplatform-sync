package com.selesse.steam.registry;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.os.Resources;
import com.selesse.steam.registry.implementation.RegistryObject;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.Test;

public class SteamRegistryTest {
    @Test
    public void readVdfParsesAFile() {
        Path path = Resources.getResource("loginusers.vdf");

        RegistryObject registryObject = SteamRegistry.getInstance().readVdf(path);

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

        Optional<RegistryObject> result = SteamRegistry.getInstance().readVdfIfPresent(path);

        assertThat(result).isEmpty();
    }

    @Test
    public void readVdfIfPresentParsesTheFileWhenItExists() {
        Path path = Resources.getResource("loginusers.vdf");

        Optional<RegistryObject> result = SteamRegistry.getInstance().readVdfIfPresent(path);

        assertThat(result).isPresent();
        assertThat(result.get().pathExists("users")).isTrue();
    }
}
