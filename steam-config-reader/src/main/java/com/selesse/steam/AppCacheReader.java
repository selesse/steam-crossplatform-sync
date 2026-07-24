package com.selesse.steam;

import com.selesse.steam.appcache.AppCache;
import com.selesse.steam.appcache.AppCacheBufferedReader;
import com.selesse.steam.registry.RegistryNotFoundException;
import com.selesse.steam.registry.SteamRegistry;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppCacheReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppCacheReader.class);

    public AppCache load() {
        return load(SteamRegistry.getInstance().getAppCachePath());
    }

    public AppCache load(Path path) {
        try {
            return new AppCacheBufferedReader(path).read();
        } catch (Exception e) {
            LOGGER.info("Unable to read app cache", e);
            throw new RegistryNotFoundException();
        }
    }
}
