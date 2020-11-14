package com.selesse.steam;

import com.selesse.os.OperatingSystems;

public abstract class SteamRegistry {
    private static SteamRegistry instance;

    public static SteamRegistry getInstance() {
        if (instance == null) {
            if (OperatingSystems.get() == OperatingSystems.OperatingSystem.MAC) {
                instance = new MacSteamRegistry();
            } else if (OperatingSystems.get() == OperatingSystems.OperatingSystem.WINDOWS) {
                instance = new WindowsSteamRegistry();
            } else {
                throw new IllegalArgumentException("No SteamRegistry defined for " + OperatingSystems.get());
            }
        }
        return instance;
    }

    public abstract long getCurrentlyRunningAppId();
}
