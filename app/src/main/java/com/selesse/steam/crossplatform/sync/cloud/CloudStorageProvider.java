package com.selesse.steam.crossplatform.sync.cloud;

import java.nio.file.Path;
import java.util.Optional;

public interface CloudStorageProvider {
    /** Unique key used in config to select this provider, e.g. {@code "google_drive"}. */
    String getName();

    /** Returns the local root of this provider's sync folder, if installed and detectable. */
    Optional<Path> getRoot();
}
