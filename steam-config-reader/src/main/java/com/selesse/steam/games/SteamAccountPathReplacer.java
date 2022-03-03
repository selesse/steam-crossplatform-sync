package com.selesse.steam.games;

import com.selesse.steam.SteamAccountId;
import com.selesse.steam.user.SteamAccountIdFinder;
import java.util.Optional;
import java.util.regex.Pattern;

public class SteamAccountPathReplacer {
    private final SteamAccountId steamAccountId;

    public SteamAccountPathReplacer() {
        steamAccountId = SteamAccountIdFinder.findIfPresent().orElse(null);
    }

    public SteamAccountPathReplacer(SteamAccountIdFinder accountIdFinder) {
        steamAccountId = accountIdFinder.find().orElse(null);
    }

    public String replace(String path) {
        String replacement = path.replace("{64BitSteamID}", sixtyFourBitReplacement())
                .replace("{Steam3AccountID}", thirtyTwoBitReplacement());
        if (replacement.contains("//")) {
            return replacement.replaceAll(Pattern.quote("//"), "/");
        }
        return replacement;
    }

    private String sixtyFourBitReplacement() {
        return Optional.ofNullable(steamAccountId).map(SteamAccountId::to64Bit).orElse("");
    }

    private String thirtyTwoBitReplacement() {
        return Optional.ofNullable(steamAccountId).map(SteamAccountId::to32Bit).orElse("");
    }
}
