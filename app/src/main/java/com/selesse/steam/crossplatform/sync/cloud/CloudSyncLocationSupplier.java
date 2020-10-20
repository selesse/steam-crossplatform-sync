package com.selesse.steam.crossplatform.sync.cloud;

import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;

import java.nio.file.Path;
import java.util.Optional;

public class CloudSyncLocationSupplier {
    // For now, this only returns Google Drive. If necessary this can be changed to support
    // other cloud storage paths.
    public static Optional<Path> get(SteamCrossplatformSyncConfig config) {
        return GoogleDrive.getDriveRoot().map(
                driveRoot -> driveRoot.toAbsolutePath().resolve(config.getCloudStorageRelativeWritePath())
        );
    }
}
