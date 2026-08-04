package com.selesse.steam.games;

import com.selesse.steam.SteamAccountId;
import java.util.Optional;

public class SteamAccountPathReplacer {
    private final SteamAccountId steamAccountId;

    /**
     * @param steamAccountId the signed-in account, or {@code null} to leave every path on its fallback
     */
    public SteamAccountPathReplacer(SteamAccountId steamAccountId) {
        this.steamAccountId = steamAccountId;
    }

    public String replace(String path, String fallbackValue) {
        return path.replace("{64BitSteamID}", sixtyFourBitReplacement(fallbackValue))
                .replace("{Steam3AccountID}", thirtyTwoBitReplacement(fallbackValue));
    }

    private String sixtyFourBitReplacement(String fallbackValue) {
        return Optional.ofNullable(steamAccountId).map(SteamAccountId::to64Bit).orElse(fallbackValue);
    }

    private String thirtyTwoBitReplacement(String fallbackValue) {
        return Optional.ofNullable(steamAccountId).map(SteamAccountId::to32Bit).orElse(fallbackValue);
    }
}
