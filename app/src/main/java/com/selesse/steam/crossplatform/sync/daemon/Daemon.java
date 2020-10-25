package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSync;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class Daemon {
    private static final Logger LOGGER = LoggerFactory.getLogger(Daemon.class);

    public static void main(String[] args) {
        SteamCrossplatformSyncConfig config = SteamCrossplatformSync.loadConfiguration();

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
        ScheduledFuture<?> scheduledFuture =
                executorService.scheduleAtFixedRate(new GameMonitor(config), 0, 5, TimeUnit.MINUTES);

        try {
            scheduledFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error scheduling daemon", e);
        }
    }
}
