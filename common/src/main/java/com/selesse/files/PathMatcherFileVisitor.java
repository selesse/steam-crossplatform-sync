package com.selesse.files;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

class PathMatcherFileVisitor extends SimpleFileVisitor<Path> {
    private final PathMatcher pathMatcher;

    private final List<Path> matchingPaths;

    public PathMatcherFileVisitor(PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
        this.matchingPaths = new ArrayList<>();
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (pathMatcher.matches(file)) {
            matchingPaths.add(file);
        }
        return FileVisitResult.CONTINUE;
    }

    // SimpleFileVisitor rethrows both of these. A save directory we can't fully read should still
    // sync the files we can, so the walk keeps going instead.
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    public List<Path> getMatchingPaths() {
        return matchingPaths;
    }
}
