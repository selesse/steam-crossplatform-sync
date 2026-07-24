package com.selesse.steam;

import com.selesse.steam.appcache.App;
import com.selesse.steam.appcache.AppCache;
import com.selesse.steam.appcache.AppCacheBufferedReader;
import com.selesse.steam.registry.RegistryNotFoundException;
import com.selesse.steam.registry.SteamRegistry;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
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

    public Optional<App> loadOne(long appId) {
        return loadOne(SteamRegistry.getInstance().getAppCachePath(), appId);
    }

    public Optional<App> loadOne(Path path, long appId) {
        try {
            return new AppCacheBufferedReader(path).readOne(appId);
        } catch (Exception e) {
            LOGGER.info("Unable to read app cache", e);
            throw new RegistryNotFoundException();
        }
    }

    public Map<Long, App> loadSome(Set<Long> appIds) {
        return loadSome(SteamRegistry.getInstance().getAppCachePath(), appIds);
    }

    public Map<Long, App> loadSome(Path path, Set<Long> appIds) {
        try {
            return new AppCacheBufferedReader(path).readSome(appIds);
        } catch (Exception e) {
            LOGGER.info("Unable to read app cache", e);
            throw new RegistryNotFoundException();
        }
    }

    public Optional<App> findFirst(Predicate<App> predicate) {
        return findFirst(SteamRegistry.getInstance().getAppCachePath(), predicate);
    }

    public Optional<App> findFirst(Path path, Predicate<App> predicate) {
        try {
            return new AppCacheBufferedReader(path).readFirst(predicate);
        } catch (Exception e) {
            LOGGER.info("Unable to read app cache", e);
            throw new RegistryNotFoundException();
        }
    }
}
