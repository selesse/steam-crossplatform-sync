package com.selesse.steam.crossplatform.sync.cloud;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class CloudSyncLocationSupplierTest {

    @Test
    public void lookupRunsOnDedicatedDaemonThreadsNotTheCommonPool() throws Exception {
        Future<String> future = CloudSyncLocationSupplier.LOOKUP_EXECUTOR.submit(
                () -> Thread.currentThread().getName());

        assertThat(future.get(5, TimeUnit.SECONDS)).startsWith("cloud-sync-location-lookup");
    }

    @Test
    public void providerRootIsResolvedOnlyOnceAcrossRepeatedCalls() {
        var provider = new CountingProvider(Optional.of(Path.of("drive-root")));
        var supplier = new CloudSyncLocationSupplier(List.of(provider));

        supplier.resolveProviderRoot(null);
        supplier.resolveProviderRoot(null);
        supplier.resolveProviderRoot(null);

        assertThat(provider.callCount()).isEqualTo(1);
    }

    @Test
    public void aFailedLookupIsAlsoMemoizedRatherThanRetried() {
        var provider = new CountingProvider(Optional.empty());
        var supplier = new CloudSyncLocationSupplier(List.of(provider));

        Optional<Path> first = supplier.resolveProviderRoot(null);
        Optional<Path> second = supplier.resolveProviderRoot(null);

        assertThat(first).isEmpty();
        assertThat(second).isEmpty();
        assertThat(provider.callCount()).isEqualTo(1);
    }

    @Test
    public void separateSuppliersDoNotShareACachedRoot() {
        var provider = new CountingProvider(Optional.of(Path.of("drive-root")));

        new CloudSyncLocationSupplier(List.of(provider)).resolveProviderRoot(null);
        new CloudSyncLocationSupplier(List.of(provider)).resolveProviderRoot(null);

        assertThat(provider.callCount()).isEqualTo(2);
    }

    @Test
    public void aProviderIsWaitedOnForAsLongAsItIsBudgetedToProbe() {
        var provider = new SlowProvider(CloudStorageProvider.LOOKUP_TIMEOUT.minusMillis(500));
        var supplier = new CloudSyncLocationSupplier(List.of(provider));

        assertThat(supplier.resolveProviderRoot(null)).contains(Path.of("drive-root"));
    }

    private static class SlowProvider implements CloudStorageProvider {
        private final Duration probeTime;

        SlowProvider(Duration probeTime) {
            this.probeTime = probeTime;
        }

        @Override
        public String getName() {
            return "slow";
        }

        @Override
        public Optional<Path> getRoot() {
            try {
                Thread.sleep(probeTime.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }
            return Optional.of(Path.of("drive-root"));
        }
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
