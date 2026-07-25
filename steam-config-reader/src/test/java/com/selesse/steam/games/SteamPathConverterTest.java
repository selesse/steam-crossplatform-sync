package com.selesse.steam.games;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SteamPathConverterTest {
    @Test
    public void convertMapsLinuxXdgDataHomeToItsSpecDefault() {
        String expected = System.getenv().getOrDefault("XDG_DATA_HOME", "~/.local/share");

        assertThat(SteamPathConverter.convert("LinuxXdgDataHome")).isEqualTo(expected);
    }

    @Test
    public void convertMapsLinuxXdgConfigHomeToItsSpecDefault() {
        String expected = System.getenv().getOrDefault("XDG_CONFIG_HOME", "~/.config");

        assertThat(SteamPathConverter.convert("LinuxXdgConfigHome")).isEqualTo(expected);
    }
}
