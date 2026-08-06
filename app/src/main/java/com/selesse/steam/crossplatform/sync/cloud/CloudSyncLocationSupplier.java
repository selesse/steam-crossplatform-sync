package com.selesse.steam.crossplatform.sync.cloud;

import com.google.common.annotations.VisibleForTesting;
import com.selesse.concurrent.IsolatedExecutors;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudSyncLocationSupplier {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudSyncLocationSupplier.class);
    // Derived from CloudStorageProvider.LOOKUP_TIMEOUT (plus slack for this method's own work),
    // rather than an independent number, so the two timeouts can't drift apart.
    @VisibleForTesting
    static final Duration LOOKUP_TIMEOUT = CloudStorageProvider.LOOKUP_TIMEOUT.plusMillis(500);
    // Isolated from ForkJoinPool.commonPool() so a provider lookup that gets stuck past its
    // timeout (e.g. a cloud-storage drive that's inaccessible in this session) can never starve
    // unrelated work — like hook execution — that also defaults to the common pool.
    @VisibleForTesting
    static final ExecutorService LOOKUP_EXECUTOR = IsolatedExecutors.newDaemonCachedPool("cloud-sync-location-lookup");

    private final List<CloudStorageProvider> providers;

    // The provider root (e.g. where Google Drive is mounted) doesn't change for the life of this
    // object. Caching it means we only ever expose ourselves once to a provider lookup that may
    // be slow or unreliable, instead of redoing it on every game-close event and every save path.
    private @Nullable Optional<Path> cachedProviderRoot;

    public CloudSyncLocationSupplier() {
        this(List.of(new GoogleDriveProvider()));
    }

    @VisibleForTesting
    CloudSyncLocationSupplier(List<CloudStorageProvider> providers) {
        this.providers = providers;
    }

    public Optional<Path> get(@Nullable String preferredProvider, Path relativeWritePath) {
        return resolveProviderRoot(preferredProvider)
                .map(root -> root.toAbsolutePath().resolve(relativeWritePath));
    }

    @VisibleForTesting
    synchronized Optional<Path> resolveProviderRoot(@Nullable String preferredProvider) {
        if (cachedProviderRoot != null) {
            return cachedProviderRoot;
        }
        Optional<Path> root = providers.stream()
                .filter(p -> preferredProvider == null || p.getName().equals(preferredProvider))
                .map(p -> {
                    var future = CompletableFuture.supplyAsync(p::getRoot, LOOKUP_EXECUTOR);
                    try {
                        return future.get(LOOKUP_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
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
}
