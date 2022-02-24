package com.selesse.steam;

import java.util.Scanner;

public record SteamAccountId(String sixtyFourBitId) {
    public SteamAccountId {
        if (!new Scanner(sixtyFourBitId).hasNextLong()) {
            throw new IllegalArgumentException(
                    String.format("Expected Steam ID to be numeric, \"%s\" wasn't", sixtyFourBitId)
            );
        }
    }

    public String to64Bit() {
        return sixtyFourBitId;
    }

    public String to32Bit() {
        return "" + to32BitId();
    }

    public long to64BitId() {
        return Long.parseLong(sixtyFourBitId);
    }

    public long to32BitId() {
        return to64BitId() - 76561197960265728L;
    }

    public boolean isPublic() {
        return to64BitId() % 2 == 1;
    }
}
