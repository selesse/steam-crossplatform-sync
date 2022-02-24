package com.selesse.steam.games;

import com.selesse.steam.SteamAccountId;
import com.selesse.steam.user.SteamAccountIdFinder;

public class SteamAccountPathReplacer {
    private final SteamAccountId steamAccountId;

    public SteamAccountPathReplacer() {
        steamAccountId = SteamAccountIdFinder.findIfPresent().orElse(null);
    }

    public String replace(String path) {
        return path.replace("/{64BitSteamID}/", sixtyFourBitReplacement())
                .replace("/{64BitSteamID}", sixtyFourBitReplacement())
                .replace("/{Steam3AccountID}/", thirtyTwoBitReplacement())
                .replace("/{Steam3AccountID}", thirtyTwoBitReplacement());
    }

    private String sixtyFourBitReplacement() {
        if (steamAccountId != null) {
            return "/" + steamAccountId.to64Bit();
        }
        return "";
    }

    private String thirtyTwoBitReplacement() {
        if (steamAccountId != null) {
            return "/" + steamAccountId.to32Bit();
        }
        return "";
    }
}
