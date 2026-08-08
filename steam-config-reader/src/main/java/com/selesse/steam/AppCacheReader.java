package com.selesse.steam;

import com.selesse.steam.appcache.App;
import com.selesse.steam.appcache.AppCache;
import com.selesse.steam.appcache.AppCacheBufferedReader;
import com.selesse.steam.registry.RegistryNotFoundException;
import com.selesse.steam.registry.SteamRegistry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppCacheReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppCacheReader.class);

    private final SteamRegistry registry;

    public AppCacheReader(SteamRegistry registry) {
        this.registry = registry;
    }

    public AppCache load() {
        return load(registry.getAppCachePath());
    }

    public AppCache load(Path path) {
        return read(path, AppCacheBufferedReader::read);
    }

    public Optional<App> loadOne(Path path, long appId) {
        return read(path, reader -> reader.readOne(appId));
    }

    public Map<Long, App> loadSome(Set<Long> appIds) {
        return loadSome(registry.getAppCachePath(), appIds);
    }

    public Map<Long, App> loadSome(Path path, Set<Long> appIds) {
        return read(path, reader -> reader.readSome(appIds));
    }

    public Optional<App> findFirst(Path path, Predicate<App> predicate) {
        return read(path, reader -> reader.readFirst(predicate));
    }

    /**
     * Every read fails the same way: the app cache is a single binary file, so a malformed or
     * missing one is not a per-entry problem worth distinguishing from any other.
     */
    private <T> T read(Path path, CacheRead<T> read) {
        if (!Files.exists(path)) {
            throw new RegistryNotFoundException(
                    "No app cache found at " + path + " - is Steam installed and has it been run at least once?");
        }
        try {
            return read.apply(new AppCacheBufferedReader(path));
        } catch (Exception e) {
            LOGGER.info("Unable to read app cache", e);
            throw new RegistryNotFoundException(
                    "Could not read app cache at " + path + " - did the cache format change?");
        }
    }

    private interface CacheRead<T> {
        T apply(AppCacheBufferedReader reader) throws Exception;
    }
}
