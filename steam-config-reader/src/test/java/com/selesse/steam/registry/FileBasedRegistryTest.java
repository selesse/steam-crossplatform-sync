package com.selesse.steam.registry;

import com.selesse.os.Resources;
import com.selesse.steam.games.SteamGameMetadata;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FileBasedRegistryTest {
    private FileBasedRegistry fileBasedRegistry;

    @Before
    public void setup() {
        fileBasedRegistry = new FileBasedRegistry() {
            @Override
            Path registryPath() {
                return Resources.getResource("registry.vdf");
            }
        };
    }

    @Test
    public void canReadTheCurrentlyRunningAppId() {
        assertThat(fileBasedRegistry.getCurrentlyRunningAppId()).isEqualTo(0);
    }

    @Test
    public void canReadInstalledAppIds() {
        assertThat(fileBasedRegistry.getInstalledAppIds()).containsExactly(
                (long) 105600,
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
                (long) 734070
        );
    }

    @Test
    public void canReadGameMetadata() {
        List<SteamGameMetadata> gameMetadata = fileBasedRegistry.getGamesMetadata();

        assertThat(gameMetadata).contains(new SteamGameMetadata(367520, "Hollow Knight", true));
    }
}