package com.selesse.steam;

import com.selesse.steam.processes.GameOverlayProcessLocator;

public class GameRunningDetector {
    public static boolean isGameCurrentlyRunning() {
        return GameOverlayProcessLocator.getRunningAppId() > 0;
    }

    public static long getCurrentlyRunningGameId() {
        return GameOverlayProcessLocator.getRunningAppId();
    }
}
