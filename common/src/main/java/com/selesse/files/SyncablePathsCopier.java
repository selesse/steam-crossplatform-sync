package com.selesse.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
            copyFile(path.toFile(), destinationPath.toFile());
        });
    }

    private static void copyFile(File source, File destination) {
        try {
            if (destination.exists()) {
                destination.delete();
            }
            createParentFolderIfNecessary(destination);
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createParentFolderIfNecessary(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }
}
