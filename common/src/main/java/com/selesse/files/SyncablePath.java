package com.selesse.files;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;

public class SyncablePath {
    private final PatternSupportedPath path;
    private PatternSupportedPath pattern;

    public SyncablePath(PatternSupportedPath baseDirectory) {
        this.path = baseDirectory;
    }

    public SyncablePath(PatternSupportedPath baseDirectory, PatternSupportedPath pattern) {
        this.path = baseDirectory;
        this.pattern = pattern;
    }

    public Path getBaseDirectory() {
        return path.toAbsolutePath();
    }

    public PathMatcher getPathMatcher() {
        if (pattern != null && pattern.hasPattern()) {
            String syntaxAndPattern = pattern.toGlobPath();
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
