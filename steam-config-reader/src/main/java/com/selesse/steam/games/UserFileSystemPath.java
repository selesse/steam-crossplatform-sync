package com.selesse.steam.games;

import static com.selesse.os.OperatingSystems.OperatingSystem;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.selesse.files.OsAgnosticPaths;
import com.selesse.os.FilePathSanitizer;
import com.selesse.steam.games.saves.SaveFileObject;
import com.selesse.steam.registry.SteamOperatingSystem;
import java.util.List;

public class UserFileSystemPath {
    private final String root;
    private final String path;

    private String pattern;
    private boolean recursive;
    private OperatingSystem platform;

    public UserFileSystemPath(String root, String path) {
        this.root = root;
        this.path = path;
    }

    public UserFileSystemPath(String root, String path, String pattern, boolean recursive, OperatingSystem platform) {
        this(root, path);
        this.pattern = pattern;
        this.recursive = recursive;
        this.platform = platform;
    }

    public static UserFileSystemPath fromSaveFile(SaveFileObject object, OperatingSystem os) {
        var userFileSystemPath = new UserFileSystemPath(object.getRoot(os), object.getPath());
        if (object.hasPattern()) {
            userFileSystemPath.pattern = object.getPattern();
        }
        if (object.hasRecursive()) {
            userFileSystemPath.recursive = object.isRecursive();
        }
        if (object.hasPlatform()) {
            if (object.getPlatform().equals("all")) {
                userFileSystemPath.platform = os;
            } else {
                userFileSystemPath.platform =
                        SteamOperatingSystem.fromString(object.getPlatform()).toOperatingSystem();
            }
        }
        return userFileSystemPath;
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
        String path = new SteamAccountPathReplacer().replace(backslashToForwardSlash(this.path), "**");
        if (pattern != null) {
            if (!path.endsWith("/")) {
                path += "/";
            }
            path += pattern;
        }
        return path;
    }

    public String getSymbolPath() {
        String convertedRoot = SteamPathConverter.convert(root);
        if (getPath().startsWith("/") || convertedRoot.endsWith("/")) {
            return backslashToForwardSlash(convertedRoot) + getPath();
        }
        return backslashToForwardSlash(convertedRoot) + "/" + getPath();
    }

    public String getLiteralPath() {
        return FilePathSanitizer.sanitize(root) + "/" + getPath();
    }

    public OperatingSystem getPlatform() {
        return platform;
    }

    public String getPattern() {
        return pattern;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public UserFileSystemPath convert(OperatingSystem target) {
        String root = getRoot();
        if (getRoot().equals("gameinstall")) {
            root = SteamInstallationPaths.getRoot(target);
        } else if (getRoot().equals("WinMyDocuments")) {
            if (target == OperatingSystem.MAC) {
                root = SteamPathConverter.convert("MacAppSupport");
            } else if (target == OperatingSystem.LINUX) {
                root = "~/.local/share";
            }
        }

        return new UserFileSystemPath(root, getPath());
    }

    private String backslashToForwardSlash(String value) {
        return OsAgnosticPaths.of(value);
    }
}
