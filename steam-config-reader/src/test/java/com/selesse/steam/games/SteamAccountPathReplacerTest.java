package com.selesse.steam.games;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.steam.user.SteamAccountIdFinder;
import java.util.Optional;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class SteamAccountPathReplacerTest {
    @Test
    public void replaceHandlesEmpty64BitIds() {
        MockedStatic<SteamAccountIdFinder> steamAccountIdFinderMockedStatic =
                Mockito.mockStatic(SteamAccountIdFinder.class);
        steamAccountIdFinderMockedStatic
                .when(SteamAccountIdFinder::findIfPresent)
                .thenReturn(Optional.empty());

        SteamAccountPathReplacer steamAccountPathReplacer = new SteamAccountPathReplacer();

        assertThat(steamAccountPathReplacer.replace("/Users/alex/torchlight/{64BitSteamID}/*", "**"))
                .isEqualTo("/Users/alex/torchlight/**/*");
    }
}
