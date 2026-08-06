package com.selesse.files;

import com.selesse.text.Splitter;
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
    private final List<String> parts;

    public PatternSupportedPath(String path) {
        this.path = OsAgnosticPaths.of(path);
        this.parts = Splitter.on("/").splitToList(this.path);
    }

    public static PatternSupportedPath of(String path) {
        return new PatternSupportedPath(path);
    }

    public static PatternSupportedPath fromPath(Path path) {
        return new PatternSupportedPath(path.toAbsolutePath().toString());
    }

    public String getFileName() {
        assert !hasPattern();
        return lastPart();
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
        return lastPart().contains("*") || lastPart().contains("?");
    }

    public String asParts() {
        return path;
    }

    public String getPattern() {
        return lastPart();
    }

    private String lastPart() {
        return parts.get(parts.size() - 1);
    }

    private boolean recursivelyIncludeEverything() {
        return lastPart().equals("*");
    }

    private Path getPathOrParentPathIfAsterisk() {
        if (hasPattern()) {
            return Path.of(String.join("/", parts.subList(0, parts.size() - 1)));
        }
        return Path.of(path);
    }

    public PatternSupportedPath relativize(PatternSupportedPath localPath) {
        if (localPath.hasPattern()) {
            return PatternSupportedPath.of(getPathOrParentPathIfAsterisk().relativize(localPath.toAbsolutePath()) + "/"
                    + localPath.getPattern());
        }
        return PatternSupportedPath.of(
                toAbsolutePath().relativize(localPath.toAbsolutePath()).toString());
    }

    public PatternSupportedPath resolve(PatternSupportedPath relativize) {
        if (relativize.asParts().startsWith("/")) {
            return PatternSupportedPath.of(path + relativize.asParts());
        }
        return PatternSupportedPath.of(path + "/" + relativize.asParts());
    }
}
