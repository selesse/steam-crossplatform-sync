package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class Daemon implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Daemon.class);
    private final SteamCrossplatformSyncConfig config;
    private final boolean fast;

    public Daemon(SteamCrossplatformSyncConfig config, boolean fast) {
        this.config = config;
        this.fast = fast;
    }

    public void run() {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
        TimeUnit timeUnitFrequency = TimeUnit.MINUTES;
        if (fast) {
            timeUnitFrequency = TimeUnit.SECONDS;
        }
        ScheduledFuture<?> scheduledFuture =
                executorService.scheduleAtFixedRate(new GameMonitor(config), 0, 5, timeUnitFrequency);

        try {
            scheduledFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error scheduling daemon", e);
        }
    }
}
