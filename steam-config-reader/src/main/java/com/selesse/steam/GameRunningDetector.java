package com.selesse.steam;

import com.selesse.steam.registry.SteamRegistry;
import com.selesse.steam.games.SteamGameMetadata;

public class GameRunningDetector {
    public static boolean isGameCurrentlyRunning() {
        return SteamRegistry.getInstance().getCurrentlyRunningAppId() > 0;
    }

    public static long getCurrentlyRunningGameId() {
        return SteamRegistry.getInstance().getCurrentlyRunningAppId();
    }

    public static SteamGameMetadata getCurrentlyRunningGameMetadata() {
        return SteamRegistry.getInstance().getGamesMetadata().stream()
                .filter(x -> x.getGameId() == SteamRegistry.getInstance().getCurrentlyRunningAppId())
                .findFirst()
                .orElseThrow();
    }
}
