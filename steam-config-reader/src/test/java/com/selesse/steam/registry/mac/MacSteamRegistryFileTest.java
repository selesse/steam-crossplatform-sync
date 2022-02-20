package com.selesse.steam.registry.mac;

import com.selesse.os.Resources;
import com.selesse.steam.games.SteamGameMetadata;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MacSteamRegistryFileTest {
    private Path testRegistryPath;

    @Before
    public void setup() {
        testRegistryPath = Resources.getResource("registry.vdf");
    }

    @Test
    public void canReadTheCurrentlyRunningAppId() {
        MacSteamRegistryFile macSteamRegistryFile = new MacSteamRegistryFile(testRegistryPath);

        assertThat(macSteamRegistryFile.getCurrentlyRunningAppId()).isEqualTo(0);
    }

    @Test
    public void canReadInstalledAppIds() {
        MacSteamRegistryFile macSteamRegistryFile = new MacSteamRegistryFile(testRegistryPath);

        assertThat(macSteamRegistryFile.getInstalledAppIds()).containsExactly(
                (long) 55040,
                (long) 105600,
                (long) 200710,
                (long) 262060,
                (long) 291650,
                (long) 349180,
                (long) 349181,
                (long) 349182,
                (long) 349183,
                (long) 349184,
                (long) 349185,
                (long) 351130,
                (long) 351131,
                (long) 351132,
                (long) 358661,
                (long) 358662,
                (long) 367520,
                (long) 367720,
                (long) 373340,
                (long) 373740,
                (long) 457140,
                (long) 607050,
                (long) 646570,
                (long) 653530,
                (long) 734070
        );
    }

    @Test
    public void canReadGameMetadata() {
        MacSteamRegistryFile steamRegistryFile = new MacSteamRegistryFile(testRegistryPath);
        List<SteamGameMetadata> gameMetadata = steamRegistryFile.getGameMetadata();

        assertThat(gameMetadata).contains(new SteamGameMetadata(367520, "Hollow Knight", true));
    }
}