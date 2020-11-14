package com.selesse.steam.mac;

import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class MacSteamRegistryFileTest {
    private Path testRegistryPath;

    @Before
    public void setup() {
        testRegistryPath = Path.of(Resources.getResource("registry.vdf").getPath());
    }

    @Test
    public void canReadTheCurrentlyRunningAppId() {
        MacSteamRegistryFile macSteamRegistryFile = new MacSteamRegistryFile(testRegistryPath);

        assertThat(macSteamRegistryFile.getCurrentlyRunningAppId()).isEqualTo(0);
    }
}