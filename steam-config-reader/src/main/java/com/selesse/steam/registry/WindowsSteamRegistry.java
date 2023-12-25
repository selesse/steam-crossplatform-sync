package com.selesse.steam.registry;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.selesse.steam.processes.GameOverlayProcessLocator;
import com.selesse.steam.registry.windows.GetGameIdFromGameOverlay;
import com.selesse.steam.registry.windows.GetInstalledAppIdsFromRegistry;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

class WindowsSteamRegistry extends SteamRegistry {
    private final LoadingCache<String, List<Long>> installedAppCache;

    public WindowsSteamRegistry() {
        this.installedAppCache = CacheBuilder.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public List<Long> load(@Nonnull String ignored) {
                        return GetInstalledAppIdsFromRegistry.get();
                    }
                });
    }

    @Override
    public long getCurrentlyRunningAppId() {
        if (GameOverlayProcessLocator.locate().isPresent()) {
            return GetGameIdFromGameOverlay.get();
        }
        return 0L;
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
