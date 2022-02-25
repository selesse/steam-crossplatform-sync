package com.selesse.steam;

import com.selesse.steam.appcache.AppCache;
import com.selesse.steam.appcache.AppCacheBufferedReader;
import com.selesse.steam.registry.RegistryNotFoundException;
import com.selesse.steam.registry.SteamRegistry;
import java.nio.file.Path;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            return submit.get(15, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.info("Interrupted while trying to read app cache", e);
            throw new RegistryNotFoundException();
        } finally {
            executorService.shutdown();
        }
    }
}
