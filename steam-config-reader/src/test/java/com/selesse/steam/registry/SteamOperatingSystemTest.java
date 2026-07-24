package com.selesse.steam.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import org.junit.Test;

public class SteamOperatingSystemTest {
    @Test
    public void fromStringParsesKnownPlatforms() {
        assertThat(SteamOperatingSystem.fromString("windows")).isEqualTo(SteamOperatingSystem.WINDOWS);
        assertThat(SteamOperatingSystem.fromString("macos")).isEqualTo(SteamOperatingSystem.MAC);
        assertThat(SteamOperatingSystem.fromString("linux")).isEqualTo(SteamOperatingSystem.LINUX);
    }

    @Test
    public void fromStringThrowsOnUnknownPlatform() {
        assertThatThrownBy(() -> SteamOperatingSystem.fromString("android"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void tryFromStringReturnsEmptyOnUnknownPlatform() {
        assertThat(SteamOperatingSystem.tryFromString("android")).isEqualTo(Optional.empty());
    }

    @Test
    public void tryFromStringReturnsKnownPlatforms() {
        assertThat(SteamOperatingSystem.tryFromString("windows")).contains(SteamOperatingSystem.WINDOWS);
    }
}
