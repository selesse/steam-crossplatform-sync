package com.selesse.steam.games;

import com.selesse.os.FilePathSanitizer;

public class UserFileSystemPath {
    private final String root;
    private final String path;

    public UserFileSystemPath(String root, String path) {
        this.root = root;
        this.path = path;
    }

    public String getRoot() {
        return root;
    }

    public String getRawPath() {
        return path;
    }

    public String getPath() {
        return path.replace("/{64BitSteamID}/", "")
                .replace("/{64BitSteamID}", "");

    }

    public String getSymbolPath() {
        String convertedRoot = SteamPathConverter.convert(root);
        if (path.startsWith("/") || convertedRoot.endsWith("/")) {
            return convertedRoot + getPath();
        }
        return convertedRoot + "/" + getPath();
    }

    public String getLiteralPath() {
        return FilePathSanitizer.sanitize(root) + "/" + getPath();
    }
}
