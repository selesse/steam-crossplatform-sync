package com.selesse.steam.crossplatform.sync.cloud;

import com.google.common.annotations.VisibleForTesting;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudSyncLocationSupplier {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudSyncLocationSupplier.class);
    private static final List<CloudStorageProvider> PROVIDERS = List.of(new GoogleDriveProvider());
    // Isolated from ForkJoinPool.commonPool() so a provider lookup that gets stuck past its
    // timeout (e.g. a cloud-storage drive that's inaccessible in this session) can never starve
    // unrelated work — like hook execution — that also defaults to the common pool.
    @VisibleForTesting
    static final ExecutorService LOOKUP_EXECUTOR = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "cloud-sync-location-lookup");
        thread.setDaemon(true);
        return thread;
    });

    // The provider root (e.g. where Google Drive is mounted) doesn't change for the life of the
    // process. Caching it means we only ever expose ourselves once to a provider lookup that may
    // be slow or unreliable, instead of redoing it on every game-close event and every save path.
    private static volatile Optional<Path> cachedProviderRoot;

    public static Optional<Path> get(SteamCrossplatformSyncConfig config) {
        return resolveProviderRoot(config, PROVIDERS)
                .map(root -> root.toAbsolutePath().resolve(config.getCloudStorageRelativeWritePath()));
    }

    @VisibleForTesting
    static synchronized Optional<Path> resolveProviderRoot(
            SteamCrossplatformSyncConfig config, List<CloudStorageProvider> providers) {
        if (cachedProviderRoot != null) {
            return cachedProviderRoot;
        }
        String preferredProvider = config.getCloudProvider();
        Optional<Path> root = providers.stream()
                .filter(p -> preferredProvider == null || p.getName().equals(preferredProvider))
                .map(p -> {
                    var future = CompletableFuture.supplyAsync(p::getRoot, LOOKUP_EXECUTOR);
                    try {
                        return future.get(1, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        LOGGER.warn("Cloud storage provider {} timed out or failed finding root", p.getName());
                        return Optional.<Path>empty();
                    }
                })
                .flatMap(Optional::stream)
                .findFirst();
        cachedProviderRoot = root;
        return root;
    }

    @VisibleForTesting
    static void resetCacheForTesting() {
        cachedProviderRoot = null;
    }
}
