package com.selesse.steam.games;

import static com.selesse.os.OperatingSystems.OperatingSystem;

import com.selesse.files.OsAgnosticPaths;
import com.selesse.steam.SteamAccountId;
import com.selesse.steam.games.saves.SaveFileObject;
import com.selesse.steam.registry.SteamOperatingSystem;

public class UserFileSystemPath {
    private final String root;
    private final String path;
    private final SteamAccountId accountId;

    private String pattern;
    private boolean recursive;
    private OperatingSystem platform;

    public UserFileSystemPath(String root, String path, SteamAccountId accountId) {
        this.root = root;
        this.path = path;
        this.accountId = accountId;
    }

    public UserFileSystemPath(
            String root,
            String path,
            SteamAccountId accountId,
            String pattern,
            boolean recursive,
            OperatingSystem platform) {
        this(root, path, accountId);
        this.pattern = pattern;
        this.recursive = recursive;
        this.platform = platform;
    }

    public static UserFileSystemPath fromSaveFile(SaveFileObject object, OperatingSystem os, SteamAccountId accountId) {
        var userFileSystemPath = new UserFileSystemPath(object.getRoot(os), object.getPath(), accountId);
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

    public String getRoot() {
        return root;
    }

    public String getRawPath() {
        return path;
    }

    public String getPath() {
        String path = new SteamAccountPathReplacer(accountId).replace(backslashToForwardSlash(this.path), "**");
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
        String path = getPath();
        if (path.startsWith("/") || convertedRoot.endsWith("/")) {
            return backslashToForwardSlash(convertedRoot) + path;
        }
        return backslashToForwardSlash(convertedRoot) + "/" + path;
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

    public UserFileSystemPath rerootForProton(String protonPrefixUserProfileRoot) {
        String convertedRoot = SteamPathConverter.convert(root);
        String rerootedRoot = convertedRoot.replace("%USERPROFILE%", protonPrefixUserProfileRoot);
        return new UserFileSystemPath(rerootedRoot, path, accountId, pattern, recursive, platform);
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

        return new UserFileSystemPath(root, getPath(), accountId);
    }

    private String backslashToForwardSlash(String value) {
        return OsAgnosticPaths.of(value);
    }
}
