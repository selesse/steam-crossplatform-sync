package com.selesse.steam;

import com.selesse.steam.registry.SteamRegistry;

public class GameRunningDetector {
    public static boolean isGameCurrentlyRunning() {
        return SteamRegistry.getInstance().getCurrentlyRunningAppId() > 0;
    }

    public static long getCurrentlyRunningGameId() {
        return SteamRegistry.getInstance().getCurrentlyRunningAppId();
    }
}
