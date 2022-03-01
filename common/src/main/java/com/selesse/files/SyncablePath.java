package com.selesse.files;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;

public class SyncablePath {
    private final Path path;
    private Path pattern;

    public SyncablePath(Path baseDirectory) {
        this.path = baseDirectory;
    }

    public SyncablePath(Path baseDirectory, Path pattern) {
        this.path = baseDirectory;
        this.pattern = pattern;
    }

    public Path getBaseDirectory() {
        return path.toAbsolutePath();
    }

    public PathMatcher getPathMatcher() {
        if (pattern != null && pattern.getFileName().toString().contains("*")) {
            String syntaxAndPattern = "glob:" + pattern.toAbsolutePath();
            return FileSystems.getDefault().getPathMatcher(syntaxAndPattern);
        }
        return path -> true;
    }

    public List<Path> getAssociatedPaths() {
        PathMatcherFileVisitor visitor = new PathMatcherFileVisitor(getPathMatcher());
        RuntimeExceptionFiles.walkFileTree(getBaseDirectory(), visitor);
        return visitor.getMatchingPaths();
    }
}
