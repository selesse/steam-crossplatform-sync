package com.selesse.steam.games;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.steam.SteamAccountId;
import com.selesse.steam.user.SteamAccountIdFinder;
import java.util.Optional;
import org.junit.Test;

public class SteamAccountPathReplacerTest {
    static class NeverFindsIds extends SteamAccountIdFinder {
        @Override
        public Optional<SteamAccountId> find() {
            return Optional.empty();
        }
    }

    @Test
    public void replaceHandlesEmpty64BitIds() {
        SteamAccountPathReplacer steamAccountPathReplacer = new SteamAccountPathReplacer(new NeverFindsIds());

        assertThat(steamAccountPathReplacer.replace("/Users/alex/{64BitSteamID}/*"))
                .isEqualTo("/Users/alex/*");
    }
}
