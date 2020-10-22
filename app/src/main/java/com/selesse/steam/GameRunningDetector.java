package com.selesse.steam;

import com.selesse.os.OperatingSystems;

public class GameRunningDetector {
    public static boolean isGameCurrentlyRunning() {
        if (OperatingSystems.get() == OperatingSystems.OperatingSystem.WINDOWS) {
            return WindowsGameRunningDetector.isGameCurrentlyRunning();
        } else if (OperatingSystems.get() == OperatingSystems.OperatingSystem.MAC) {
            return MacGameRunningDetector.isGameCurrentlyRunning();
        } else {
            throw new RuntimeException("Invalid OS");
        }
    }
}
