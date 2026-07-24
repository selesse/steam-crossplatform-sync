package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.AppCacheReader;
import com.selesse.steam.appcache.AppCache;
import com.selesse.steam.crossplatform.sync.SteamCrossplatformSyncContext;
import com.selesse.steam.registry.RegistryNotFoundException;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Daemon implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Daemon.class);
    private final SteamCrossplatformSyncContext context;
    private final boolean fast;

    public Daemon(SteamCrossplatformSyncContext context, boolean fast) {
        this.context = context;
        this.fast = fast;
    }

    public void run() {
        ensureAppCacheIsReadable();
        initializeDaemon();
    }

    private void ensureAppCacheIsReadable() {
        AppCacheReader appCacheReader = new AppCacheReader();
        try {
            AppCache appCache = appCacheReader.load();
            LOGGER.info("Found {} games in the app cache", appCache.getApps().size());
        } catch (RegistryNotFoundException e) {
            LOGGER.error("App cache could not be read - did the cache format change?", e);
            throw e;
        }
    }

    private void initializeDaemon() {
        LOGGER.info("Initializing game monitor daemon");
        try (ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2)) {
            int period = 30;
            TimeUnit timeUnitFrequency = TimeUnit.SECONDS;
            if (fast) {
                period = 5;
                timeUnitFrequency = TimeUnit.SECONDS;
            }
            GameMonitor command = new GameMonitor(context);
            ScheduledFuture<?> scheduledFuture = executorService.scheduleAtFixedRate(
                    getExceptionTolerantRunnable(command), 0, period, timeUnitFrequency);
            executorService.scheduleAtFixedRate(
                    getExceptionTolerantRunnable(new GameNameEnricher(context)), 0, 1, TimeUnit.HOURS);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("Shutting down game monitor daemon");
                command.run();
            }));

            try {
                scheduledFuture.get();
            } catch (InterruptedException e) {
                LOGGER.error("Error scheduling daemon", e);
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                LOGGER.error("Error scheduling daemon", e);
            }
        }
    }

    private Runnable getExceptionTolerantRunnable(Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (RuntimeException e) {
                LOGGER.info("Execution exception", e);
            }
        };
    }
}
