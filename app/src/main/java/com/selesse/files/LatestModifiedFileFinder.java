package com.selesse.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class LatestModifiedFileFinder {
    private final Path root;

    public LatestModifiedFileFinder(Path root) {
        this.root = root;
    }

    public LastModifiedResult getLastModified() {
        LatestModifiedFileVisitor visitor = new LatestModifiedFileVisitor();
        try {
            Files.walkFileTree(root, visitor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (visitor.getLatestLastModified() == -1) {
            return LastModifiedResult.doesNotExist();
        }
        Instant instant = Instant.ofEpochMilli(visitor.getLatestLastModified());
        return LastModifiedResult.of(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }
}
