package com.selesse.files;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

public class LatestModifiedFileFinder {
    private final SyncablePath root;

    public LatestModifiedFileFinder(SyncablePath root) {
        this.root = root;
    }

    /** When the newest matching file was last modified, or empty when there are no matches. */
    public Optional<LocalDateTime> getLastModified() {
        LatestModifiedFileVisitor visitor = new LatestModifiedFileVisitor(root.getPathMatcher());
        RuntimeExceptionFiles.walkFileTree(root.getBaseDirectory(), visitor);
        if (visitor.getLatestLastModified() == -1) {
            return Optional.empty();
        }
        Instant instant = Instant.ofEpochMilli(visitor.getLatestLastModified());
        return Optional.of(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }
}
