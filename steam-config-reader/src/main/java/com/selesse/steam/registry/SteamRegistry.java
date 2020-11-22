package com.selesse.steam.registry;

import com.google.common.collect.Lists;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.steamcmd.games.SteamGameMetadata;

import java.util.List;

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

    public List<Long> getInstalledAppIds() {
        return Lists.newArrayList();
    }

    public List<SteamGameMetadata> getGameMetadata() {
        return Lists.newArrayList();
    }
}
