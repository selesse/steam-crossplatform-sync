package com.selesse.files;

import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncablePathsCopier {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncablePathsCopier.class);

    public static void copy(SyncablePath source, SyncablePath destination) {
        List<Path> files = source.getAssociatedPaths();
        files.forEach(path -> {
            Path relativePath = source.getBaseDirectory().relativize(path);
            Path destinationPath = Path.of(destination.getBaseDirectory().toString(), relativePath.toString());
            LOGGER.info("Copying {} to {}", path.toFile(), destinationPath.toFile());
            DirectoryCopier.copyFile(path.toFile(), destinationPath.toFile());
        });
    }
}
