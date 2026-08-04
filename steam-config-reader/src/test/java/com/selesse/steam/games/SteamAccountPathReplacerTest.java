package com.selesse.steam.games;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SteamAccountPathReplacerTest {
    @Test
    public void replaceHandlesEmpty64BitIds() {
        SteamAccountPathReplacer steamAccountPathReplacer = new SteamAccountPathReplacer(null);

        assertThat(steamAccountPathReplacer.replace("/Users/alex/torchlight/{64BitSteamID}/*", "**"))
                .isEqualTo("/Users/alex/torchlight/**/*");
    }
}
