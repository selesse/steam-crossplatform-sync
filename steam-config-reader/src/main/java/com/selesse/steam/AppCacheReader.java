package com.selesse.steam;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.selesse.steam.appcache.AppCache;
import com.selesse.steam.appcache.AppCacheBufferedReader;
import com.selesse.steam.registry.RegistryNotFoundException;
import com.selesse.steam.registry.SteamRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AppCacheReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppCacheReader.class);

    public AppCache load() {
        return load(SteamRegistry.getInstance().getAppCachePath());
    }

    @SuppressWarnings("UnstableApiUsage")
    public AppCache load(Path path) {
        AppCacheBufferedReader appCacheBufferedReader = new AppCacheBufferedReader(path);

        TimeLimiter timeLimiter = SimpleTimeLimiter.create(Executors.newSingleThreadExecutor());
        try {
            return timeLimiter.callWithTimeout(appCacheBufferedReader, 5, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            LOGGER.error("Unable to parse app cache", e);
            throw new RegistryNotFoundException();
        }
    }
}
