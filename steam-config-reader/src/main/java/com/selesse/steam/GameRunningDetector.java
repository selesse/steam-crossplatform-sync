package com.selesse.steam;

import com.selesse.steam.registry.SteamRegistry;
import com.selesse.steam.steamcmd.games.SteamGameMetadata;

public class GameRunningDetector {
    public static boolean isGameCurrentlyRunning() {
        return SteamRegistry.getInstance().getCurrentlyRunningAppId() > 0;
    }

    public static SteamGameMetadata getCurrentlyRunningGameMetadata() {
        return SteamRegistry.getInstance().getGameMetadata().stream()
                .filter(x -> x.getGameId() == SteamRegistry.getInstance().getCurrentlyRunningAppId())
                .findFirst()
                .orElseThrow();
    }
}
