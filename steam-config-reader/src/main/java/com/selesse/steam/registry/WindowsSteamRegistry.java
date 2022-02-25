package com.selesse.steam.registry;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.selesse.steam.registry.windows.GetCurrentlyRunningGameIdViaRegistry;
import com.selesse.steam.registry.windows.GetGameIdFromGameOverlay;
import com.selesse.steam.registry.windows.GetInstalledAppIdsFromRegistry;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WindowsSteamRegistry extends SteamRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(WindowsSteamRegistry.class);
    private final LoadingCache<String, List<Long>> installedAppCache;

    public WindowsSteamRegistry() {
        this.installedAppCache = CacheBuilder.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public List<Long> load(String ignored) {
                        return GetInstalledAppIdsFromRegistry.get();
                    }
                });
    }

    @Override
    public long getCurrentlyRunningAppId() {
        long registryValue = GetCurrentlyRunningGameIdViaRegistry.get();
        if (registryValue == 0 && hasGameOverlayProcessRunning()) {
            // For whatever reason, sometimes Steam's registry value is incorrectly set to 0.
            // This makes us more robust at the expense of being slightly more expensive to run.
            LOGGER.info("Steam's registry said the app ID was 0, but we detected the GameOverlay");
            return GetGameIdFromGameOverlay.get();
        }
        return registryValue;
    }

    @Override
    public List<Long> getInstalledAppIds() {
        try {
            return installedAppCache.get("cache");
        } catch (ExecutionException e) {
            return GetInstalledAppIdsFromRegistry.get();
        }
    }

    private boolean hasGameOverlayProcessRunning() {
        return ProcessHandle.allProcesses()
                .filter(ProcessHandle::isAlive)
                .anyMatch(process -> process.info().command().orElseThrow().contains("Steam\\GameOverlayUI"));
    }
}
