package com.selesse.steam.crossplatform.sync;

import com.selesse.files.DirectoryCopier;
import com.selesse.files.LastModifiedResult;
import com.selesse.files.LatestModifiedFileFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class SyncResolver {
    private static Logger LOGGER = LoggerFactory.getLogger(SyncResolver.class);

    public static void resolve(Path local, Path sync) {
        LatestModifiedFileFinder latestModifiedLocalFinder = new LatestModifiedFileFinder(local);
        LatestModifiedFileFinder latestModifiedSyncFinder = new LatestModifiedFileFinder(sync);

        LastModifiedResult localResult = latestModifiedLocalFinder.getLastModified();
        LastModifiedResult syncResult = latestModifiedSyncFinder.getLastModified();

        if (localResult.exists() && syncResult.exists()) {
            LOGGER.info("Both directories exist, comparing...");
            LocalDateTime lastModifiedLocal = localResult.getLastModified();
            LocalDateTime lastModifiedSync = syncResult.getLastModified();

            if (lastModifiedLocal.equals(lastModifiedSync)) {
                LOGGER.info("Up to date");
            } else if (lastModifiedLocal.isAfter(lastModifiedSync)) {
                LOGGER.info("Copying local into sync");
                copyFiles(local, sync);
            } else if (lastModifiedSync.isAfter(lastModifiedLocal)) {
                LOGGER.info("Copying sync into local");
                copyFiles(sync, local);
            }
        }
        if (localResult.exists() && !syncResult.exists()) {
            LOGGER.info("Local exists, sync does not");
            LOGGER.info("Copying local into sync");
            copyFiles(local, sync);
        }
        if (!localResult.exists() && syncResult.exists()) {
            LOGGER.info("Local does not exist, sync exists");
            copyFiles(sync, local);
        }
        if (!localResult.exists() && !syncResult.exists()) {
            LOGGER.info("Neither exist");
        }
    }

    private static void copyFiles(Path a, Path b) {
        try {
            DirectoryCopier.copyFileOrFolder(a.toFile(), b.toFile());
        } catch (IOException e) {
            LOGGER.error("Error trying to copy {} into {}", a.toAbsolutePath(), b.toAbsolutePath());
            throw new RuntimeException(e);
        }
    }
}
