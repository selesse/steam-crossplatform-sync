package com.selesse.steam.crossplatform.sync.cloud;

import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class CloudSyncLocationSupplier {
    private static final List<CloudStorageProvider> PROVIDERS = List.of(new GoogleDriveProvider());

    public static Optional<Path> get(SteamCrossplatformSyncConfig config) {
        String preferredProvider = config.getCloudProvider();
        return PROVIDERS.stream()
                .filter(p -> preferredProvider == null || p.getName().equals(preferredProvider))
                .map(CloudStorageProvider::getRoot)
                .flatMap(Optional::stream)
                .findFirst()
                .map(root -> root.toAbsolutePath().resolve(config.getCloudStorageRelativeWritePath()));
    }
}
