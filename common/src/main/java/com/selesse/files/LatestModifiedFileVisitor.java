package com.selesse.files;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

class LatestModifiedFileVisitor extends SimpleFileVisitor<Path> {
    private final PathMatcher pathMatcher;
    private long latestLastModified = -1;

    public LatestModifiedFileVisitor(PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    public long getLatestLastModified() {
        return latestLastModified;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (pathMatcher.matches(file)) {
            latestLastModified = Math.max(attrs.lastModifiedTime().toMillis(), latestLastModified);
        }
        return FileVisitResult.CONTINUE;
    }

    // SimpleFileVisitor rethrows both of these. A save directory we can't fully read should still
    // report the newest file we could see, so the walk keeps going instead.
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return FileVisitResult.CONTINUE;
    }
}
