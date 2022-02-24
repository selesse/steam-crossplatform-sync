package com.selesse.steam.games;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.selesse.os.FilePathSanitizer;

import java.util.List;
import java.util.regex.Pattern;

public class UserFileSystemPath {
    private final String root;
    private final String path;

    public UserFileSystemPath(String root, String path) {
        this.root = root;
        this.path = path;
    }

    public UserFileSystemPath(String fullPath) {
        List<String> parts = Splitter.on("/").splitToList(fullPath);
        this.root = Joiner.on("/").join(parts.subList(0, parts.size() - 1));
        this.path = Iterables.getLast(parts);
    }

    public String getRoot() {
        return root;
    }

    public String getRawPath() {
        return path;
    }

    public String getPath() {
        return new SteamAccountPathReplacer().replace(path);
    }

    public String getSymbolPath() {
        String convertedRoot = SteamPathConverter.convert(root);
        convertedRoot = convertedRoot.replaceAll(Pattern.quote("\\"), "/");
        if (path.startsWith("/") || convertedRoot.endsWith("/")) {
            return convertedRoot + getPath();
        }
        return convertedRoot + "/" + getPath();
    }

    public String getLiteralPath() {
        return FilePathSanitizer.sanitize(root) + "/" + getPath();
    }
}
