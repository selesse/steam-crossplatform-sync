package com.selesse.steam;

import com.selesse.steam.registry.SteamRegistry;

public class GameRunningDetector {
    public static boolean isGameCurrentlyRunning() {
        return SteamRegistry.getInstance().getCurrentlyRunningAppId() > 0;
    }
}
