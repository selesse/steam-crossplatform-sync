package com.selesse.steam.registry;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.steamcmd.games.SteamGameMetadata;

import java.util.List;

public abstract class SteamRegistry {
    public static SteamRegistry getInstance() {
        return switch (OperatingSystems.get()) {
            case MAC -> new MacSteamRegistry();
            case WINDOWS -> new WindowsSteamRegistry();
            case LINUX -> new LinuxSteamRegistry();
        };
    }

    public abstract long getCurrentlyRunningAppId();
    public abstract List<Long> getInstalledAppIds();
    public abstract List<SteamGameMetadata> getGameMetadata();
    public abstract SteamGameMetadata getGameMetadata(Long gameId);
}
