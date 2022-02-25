package com.selesse.steam;

import java.util.Scanner;

public record SteamAccountId(String sixtyFourBitId) {
    private static final Long MAGIC_CONVERSION_NUMBER = 76561197960265728L;

    public SteamAccountId {
        if (!new Scanner(sixtyFourBitId).hasNextLong()) {
            throw new IllegalArgumentException(
                    String.format("Expected Steam ID to be numeric, \"%s\" wasn't", sixtyFourBitId));
        }
    }

    public static SteamAccountId from32Bit(long thirtyTwoBit) {
        return new SteamAccountId("" + (thirtyTwoBit + MAGIC_CONVERSION_NUMBER));
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
        return to64BitId() - MAGIC_CONVERSION_NUMBER;
    }

    public boolean isPublic() {
        return to64BitId() % 2 == 1;
    }
}
