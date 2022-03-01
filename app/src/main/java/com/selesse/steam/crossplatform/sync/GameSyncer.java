package com.selesse.steam.crossplatform.sync;

import com.selesse.files.*;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameSyncer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameSyncer.class);

    public static void sync(SyncablePath local, SyncablePath cloudSyncPath) {
        LatestModifiedFileFinder latestModifiedLocalFinder = new LatestModifiedFileFinder(local);
        LatestModifiedFileFinder latestModifiedSyncFinder = new LatestModifiedFileFinder(cloudSyncPath);

        LastModifiedResult localResult = latestModifiedLocalFinder.getLastModified();
        LastModifiedResult syncResult = latestModifiedSyncFinder.getLastModified();

        if (localResult.exists() && syncResult.exists()) {
            LOGGER.info("Both directories exist, comparing...");
            LocalDateTime lastModifiedLocal = localResult.getLastModified();
            LocalDateTime lastModifiedSync = syncResult.getLastModified();

            if (lastModifiedLocal.equals(lastModifiedSync)) {
                LOGGER.info("Up to date");
            } else if (lastModifiedLocal.isAfter(lastModifiedSync)) {
                LOGGER.info("Copying local into cloudSyncPath");
                copyFiles(local, cloudSyncPath);
            } else if (lastModifiedSync.isAfter(lastModifiedLocal)) {
                LOGGER.info("Copying cloudSyncPath into local");
                copyFiles(cloudSyncPath, local);
            }
        }
        if (localResult.exists() && !syncResult.exists()) {
            LOGGER.info("Local exists, cloudSyncPath does not");
            LOGGER.info("Copying local into cloudSyncPath");
            copyFiles(local, cloudSyncPath);
        }
        if (!localResult.exists() && syncResult.exists()) {
            LOGGER.info("Local does not exist, cloudSyncPath exists");
            LOGGER.info("Copying cloudSyncPath into local");
            copyFiles(cloudSyncPath, local);
        }
        if (!localResult.exists() && !syncResult.exists()) {
            LOGGER.info("Neither exist");
        }
    }

    private static void copyFiles(SyncablePath source, SyncablePath destination) {
        SyncablePathsCopier.copy(source, destination);
    }
}
