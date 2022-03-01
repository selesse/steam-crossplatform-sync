package com.selesse.files;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class LatestModifiedFileFinder {
    private final SyncablePath root;

    public LatestModifiedFileFinder(SyncablePath root) {
        this.root = root;
    }

    public LastModifiedResult getLastModified() {
        LatestModifiedFileVisitor visitor = new LatestModifiedFileVisitor(root.getPathMatcher());
        RuntimeExceptionFiles.walkFileTree(root.getBaseDirectory(), visitor);
        if (visitor.getLatestLastModified() == -1) {
            return LastModifiedResult.doesNotExist();
        }
        Instant instant = Instant.ofEpochMilli(visitor.getLatestLastModified());
        return LastModifiedResult.of(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }
}
