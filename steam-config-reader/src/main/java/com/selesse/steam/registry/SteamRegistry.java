package com.selesse.steam.registry;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.steamcmd.games.SteamGameMetadata;

import java.util.List;

public abstract class SteamRegistry {
    public static SteamRegistry getInstance() {
        if (OperatingSystems.get() == OperatingSystems.OperatingSystem.MAC) {
            return new MacSteamRegistry();
        } else if (OperatingSystems.get() == OperatingSystems.OperatingSystem.WINDOWS) {
            return new WindowsSteamRegistry();
        } else {
            throw new IllegalArgumentException("No SteamRegistry defined for " + OperatingSystems.get());
        }
    }

    public abstract long getCurrentlyRunningAppId();
    public abstract List<Long> getInstalledAppIds();
    public abstract List<SteamGameMetadata> getGameMetadata();
    public abstract SteamGameMetadata getGameMetadata(Long gameId);
}
