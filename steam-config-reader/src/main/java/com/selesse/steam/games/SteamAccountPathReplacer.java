package com.selesse.steam.games;

import com.selesse.steam.SteamAccountId;
import com.selesse.steam.user.SteamAccountIdFinder;
import java.util.Optional;

public class SteamAccountPathReplacer {
    private final SteamAccountId steamAccountId;

    public SteamAccountPathReplacer() {
        steamAccountId = SteamAccountIdFinder.findIfPresent().orElse(null);
    }

    public String replace(String path) {
        return path.replace("{64BitSteamID}", sixtyFourBitReplacement())
                .replace("{Steam3AccountID}", thirtyTwoBitReplacement());
    }

    private String sixtyFourBitReplacement() {
        return Optional.ofNullable(steamAccountId).map(SteamAccountId::to64Bit).orElse("");
    }

    private String thirtyTwoBitReplacement() {
        return Optional.ofNullable(steamAccountId).map(SteamAccountId::to32Bit).orElse("");
    }
}
