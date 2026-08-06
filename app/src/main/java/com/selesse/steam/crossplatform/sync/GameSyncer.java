package com.selesse.steam.crossplatform.sync;

import com.selesse.files.LatestModifiedFileFinder;
import com.selesse.files.SyncablePath;
import com.selesse.files.SyncablePathsCopier;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameSyncer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameSyncer.class);

    public static void sync(SyncablePath local, SyncablePath cloudSyncPath) {
        Optional<LocalDateTime> localResult = new LatestModifiedFileFinder(local).getLastModified();
        Optional<LocalDateTime> syncResult = new LatestModifiedFileFinder(cloudSyncPath).getLastModified();

        if (localResult.isPresent() && syncResult.isPresent()) {
            LOGGER.info("Both directories exist, comparing...");
            LocalDateTime lastModifiedLocal = localResult.get();
            LocalDateTime lastModifiedSync = syncResult.get();

            if (lastModifiedLocal.equals(lastModifiedSync)) {
                LOGGER.info("Up to date");
            } else if (lastModifiedLocal.isAfter(lastModifiedSync)) {
                LOGGER.info("Copying local into cloudSyncPath");
                SyncablePathsCopier.copy(local, cloudSyncPath);
            } else {
                LOGGER.info("Copying cloudSyncPath into local");
                SyncablePathsCopier.copy(cloudSyncPath, local);
            }
        } else if (localResult.isPresent()) {
            LOGGER.info("Local exists, cloudSyncPath does not");
            LOGGER.info("Copying local into cloudSyncPath");
            SyncablePathsCopier.copy(local, cloudSyncPath);
        } else if (syncResult.isPresent()) {
            LOGGER.info("Local does not exist, cloudSyncPath exists");
            LOGGER.info("Copying cloudSyncPath into local");
            SyncablePathsCopier.copy(cloudSyncPath, local);
        } else {
            LOGGER.info("Neither exist");
        }
    }
}
