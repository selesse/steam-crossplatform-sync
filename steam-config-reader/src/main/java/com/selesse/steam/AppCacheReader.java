package com.selesse.steam;

import com.selesse.steam.appcache.AppCache;
import com.selesse.steam.appcache.AppCacheBufferedReader;
import com.selesse.steam.registry.RegistryNotFoundException;
import com.selesse.steam.registry.SteamRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.*;

public class AppCacheReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppCacheReader.class);

    public AppCache load() {
        return load(SteamRegistry.getInstance().getAppCachePath());
    }

    public AppCache load(Path path) {
        AppCacheBufferedReader appCacheBufferedReader = new AppCacheBufferedReader(path);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<AppCache> submit = executorService.submit(appCacheBufferedReader);
        try {
            AppCache appCache = submit.get(5, TimeUnit.SECONDS);
            executorService.shutdown();
            return appCache;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.info("Interrupted while trying to read app cache", e);
            throw new RegistryNotFoundException();
        }
    }
}
