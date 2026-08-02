package com.selesse.steam;

import com.selesse.steam.processes.GameOverlayProcessLocator;
import java.util.OptionalLong;

public class GameRunningDetector {
    public static OptionalLong getCurrentlyRunningGameId() {
        long appId = GameOverlayProcessLocator.getRunningAppId();
        return appId > 0 ? OptionalLong.of(appId) : OptionalLong.empty();
    }
}
