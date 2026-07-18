package com.selesse.steam.crossplatform.sync.cloud;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CloudSyncLocationSupplierTest {

    @Before
    public void resetCache() {
        CloudSyncLocationSupplier.resetCacheForTesting();
    }

    @After
    public void clearCache() {
        CloudSyncLocationSupplier.resetCacheForTesting();
    }

    @Test
    public void lookupRunsOnDedicatedDaemonThreadsNotTheCommonPool() throws Exception {
        Future<String> future = CloudSyncLocationSupplier.LOOKUP_EXECUTOR.submit(
                () -> Thread.currentThread().getName());

        assertThat(future.get(5, TimeUnit.SECONDS)).startsWith("cloud-sync-location-lookup");
    }

    @Test
    public void providerRootIsResolvedOnlyOnceAcrossRepeatedCalls() {
        var provider = new CountingProvider(Optional.of(Path.of("drive-root")));
        var config = mock(SteamCrossplatformSyncConfig.class);
        doReturn(Path.of("games")).when(config).getCloudStorageRelativeWritePath();

        CloudSyncLocationSupplier.resolveProviderRoot(config, List.of(provider));
        CloudSyncLocationSupplier.resolveProviderRoot(config, List.of(provider));
        CloudSyncLocationSupplier.resolveProviderRoot(config, List.of(provider));

        assertThat(provider.callCount()).isEqualTo(1);
    }

    @Test
    public void aFailedLookupIsAlsoMemoizedRatherThanRetried() {
        var provider = new CountingProvider(Optional.empty());
        var config = mock(SteamCrossplatformSyncConfig.class);
        doReturn(Path.of("games")).when(config).getCloudStorageRelativeWritePath();

        Optional<Path> first = CloudSyncLocationSupplier.resolveProviderRoot(config, List.of(provider));
        Optional<Path> second = CloudSyncLocationSupplier.resolveProviderRoot(config, List.of(provider));

        assertThat(first).isEmpty();
        assertThat(second).isEmpty();
        assertThat(provider.callCount()).isEqualTo(1);
    }

    private static class CountingProvider implements CloudStorageProvider {
        private final AtomicInteger callCount = new AtomicInteger();
        private final Optional<Path> root;

        CountingProvider(Optional<Path> root) {
            this.root = root;
        }

        int callCount() {
            return callCount.get();
        }

        @Override
        public String getName() {
            return "counting";
        }

        @Override
        public Optional<Path> getRoot() {
            callCount.incrementAndGet();
            return root;
        }
    }
}
