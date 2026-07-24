package com.selesse.steam.crossplatform.sync.cloud;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

public interface CloudStorageProvider {
    /**
     * Total time an implementation's {@link #getRoot()} may spend on blocking probes. Callers
     * budget their own timeout around this, so an implementation that ignores it can outlast
     * what the caller is willing to wait for.
     */
    Duration LOOKUP_TIMEOUT = Duration.ofSeconds(2);

    /** Unique key used in config to select this provider, e.g. {@code "google_drive"}. */
    String getName();

    /** Returns the local root of this provider's sync folder, if installed and detectable. */
    Optional<Path> getRoot();
}
