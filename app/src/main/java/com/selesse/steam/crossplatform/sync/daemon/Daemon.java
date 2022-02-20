package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.crossplatform.sync.SteamCrossplatformSyncContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class Daemon implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Daemon.class);
    private final SteamCrossplatformSyncContext context;
    private final boolean fast;

    public Daemon(SteamCrossplatformSyncContext context, boolean fast) {
        this.context = context;
        this.fast = fast;
    }

    public void run() {
        LOGGER.info("Initializing game monitor daemon");
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
        int period = 1;
        TimeUnit timeUnitFrequency = TimeUnit.MINUTES;
        if (fast) {
            period = 5;
            timeUnitFrequency = TimeUnit.SECONDS;
        }
        GameMonitor command = new GameMonitor(context);
        ScheduledFuture<?> scheduledFuture =
                executorService.scheduleAtFixedRate(command, 0, period, timeUnitFrequency);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down game monitor daemon");
            command.run();
        }));

        try {
            scheduledFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error scheduling daemon", e);
        }
    }
}
