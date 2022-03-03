package com.selesse.files;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.nio.file.Path;
import java.util.List;

/**
 * A {@link Path} wrapper that supports * in its path. This is supported just fine on OS X / Linux, but
 * Windows doesn't accept * characters and throws an exception when we try to initialize a path with an asterisk
 * in it.
 *
 * <pre>{@code var x = PatternSupportedPath.of("C:\\Users\\alex\\*")
 * x.toGlobPath(); // glob:C:/Users/alex/**
 * x = PatternSupportedPath.of("/Users/alex/*.txt");
 * x.toGlobPath(); // glob:/Users/alex/*.txt
 * x.getParent(); // /Users/alex
 * }
 * </pre>
 */
public class PatternSupportedPath {
    private final String path;

    public PatternSupportedPath(String path) {
        this.path = OsAgnosticPaths.of(path);
    }

    public static PatternSupportedPath of(String path) {
        return new PatternSupportedPath(path);
    }

    public static PatternSupportedPath fromPath(Path path) {
        return new PatternSupportedPath(path.toAbsolutePath().toString());
    }

    public PatternSupportedPath getParent() {
        if (hasPattern()) {
            return fromPath(getPathOrParentPathIfAsterisk());
        }
        return fromPath(Path.of(path).getParent());
    }

    public boolean endsWith(String endsWith) {
        return getPathOrParentPathIfAsterisk().endsWith(endsWith);
    }

    public String toGlobPath() {
        if (hasPattern() && recursivelyIncludeEverything()) {
            return "glob:" + path + "*";
        }
        return "glob:" + path;
    }

    public Path toAbsolutePath() {
        return getPathOrParentPathIfAsterisk().toAbsolutePath();
    }

    public boolean hasPattern() {
        List<String> parts = Splitter.on("/").splitToList(path);
        String last = Iterables.getLast(parts);
        return last.contains("*") || last.contains("?");
    }

    private boolean recursivelyIncludeEverything() {
        List<String> parts = Splitter.on("/").splitToList(path);
        return Iterables.getLast(parts).equals("*");
    }

    private Path getPathOrParentPathIfAsterisk() {
        if (hasPattern()) {
            List<String> parts = Splitter.on("/").splitToList(path);
            return Path.of(Joiner.on("/").join(parts.subList(0, parts.size() - 1)));
        }
        return Path.of(path);
    }
}
