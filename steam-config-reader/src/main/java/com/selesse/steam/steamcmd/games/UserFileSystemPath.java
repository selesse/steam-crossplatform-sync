package com.selesse.steam.steamcmd.games;

import com.selesse.os.FilePathSanitizer;

public class UserFileSystemPath {
    private final String root;
    private final String path;

    public UserFileSystemPath(String root, String path) {
        this.root = root;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String getSymbolPath() {
        String convert = SteamPathConverter.convert(root);
        if (path.startsWith("/") || convert.endsWith("/")) {
            return convert + path;
        }
        return convert + "/" + path;
    }

    public String getLiteralPath() {
        return FilePathSanitizer.sanitize(root) + "/" + path;
    }
}
