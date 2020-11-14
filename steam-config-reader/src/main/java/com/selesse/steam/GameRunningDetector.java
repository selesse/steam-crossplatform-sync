package com.selesse.steam;

public class GameRunningDetector {
    public static boolean isGameCurrentlyRunning() {
        return SteamRegistry.getInstance().getCurrentlyRunningAppId() > 0;
    }
}
