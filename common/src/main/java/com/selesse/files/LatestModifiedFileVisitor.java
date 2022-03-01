package com.selesse.files;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;

class LatestModifiedFileVisitor implements FileVisitor<Path> {
    private final PathMatcher pathMatcher;
    private long latestLastModified = -1;

    public LatestModifiedFileVisitor(PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    public long getLatestLastModified() {
        return latestLastModified;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (pathMatcher.matches(file)) {
            latestLastModified = Math.max(attrs.lastModifiedTime().toMillis(), latestLastModified);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return FileVisitResult.CONTINUE;
    }
}
