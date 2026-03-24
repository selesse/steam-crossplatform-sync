package com.selesse.steam.registry;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.selesse.steam.registry.windows.GetInstalledAppIdsFromRegistry;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.NonNull;

class WindowsSteamRegistry extends SteamRegistry {
    private final LoadingCache<String, List<Long>> installedAppCache;

    public WindowsSteamRegistry() {
        this.installedAppCache = CacheBuilder.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public List<Long> load(@NonNull String ignored) {
                        return GetInstalledAppIdsFromRegistry.get();
                    }
                });
    }

    @Override
    public List<Long> getInstalledAppIds() {
        try {
            return installedAppCache.get("cache");
        } catch (ExecutionException e) {
            return GetInstalledAppIdsFromRegistry.get();
        }
    }
}
