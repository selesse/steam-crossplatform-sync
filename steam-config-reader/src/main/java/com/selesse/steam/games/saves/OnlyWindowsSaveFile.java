package com.selesse.steam.games.saves;

import com.selesse.steam.games.UserFileSystemPath;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryStore;

public class OnlyWindowsSaveFile extends SaveFile {
    public OnlyWindowsSaveFile(RegistryStore gameRegistry) {
        super(gameRegistry);
    }

    @Override
    public boolean applies() {
        boolean hasSaveFiles = ufs.pathExists("savefiles");
        boolean hasOverrides = ufs.pathExists("overrides");
        if (hasSaveFiles && !hasOverrides) {
            RegistryObject saveFiles = ufs.getObjectValueAsObject("savefiles");
            return saveFiles.getKeys().size() == 1;
        }
        return false;
    }

    @Override
    public UserFileSystemPath getMacInfo() {
        String root = "~/Library/Application Support";
        String path = getWindowsInfo().getPath();
        return new UserFileSystemPath(root, path);
    }

    @Override
    public UserFileSystemPath getWindowsInfo() {
        String root = ufs.getObjectValueAsString("savefiles/0/root").getValue();
        String path = ufs.getObjectValueAsString("savefiles/0/path").getValue();
        return new UserFileSystemPath(root, path);
    }

    @Override
    public UserFileSystemPath getLinuxInfo() {
        String root = "~";
        String path = getWindowsInfo().getPath();
        return new UserFileSystemPath(root, path);
    }
}
