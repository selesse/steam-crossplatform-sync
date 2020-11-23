package com.selesse.steam.steamcmd.games;

import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryStore;

public class OnlyWindowsSaveFile implements SaveFile {
    private final RegistryStore registryStore;

    public OnlyWindowsSaveFile(RegistryStore registryStore) {
        this.registryStore = registryStore;
    }

    @Override
    public boolean applies() {
        boolean hasSaveFiles = registryStore.pathExists("savefiles");
        boolean hasOverrides = registryStore.pathExists("overrides");
        if (hasSaveFiles && !hasOverrides) {
            RegistryObject saveFiles = registryStore.getObjectValueAsObject("savefiles");
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
        String root = registryStore.getObjectValueAsString("savefiles/0/root").getValue();
        String path = registryStore.getObjectValueAsString("savefiles/0/path").getValue();
        return new UserFileSystemPath(root, path);
    }
}
