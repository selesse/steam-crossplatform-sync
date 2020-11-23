package com.selesse.steam.steamcmd.games;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.registry.implementation.RegistryStore;

public class GameInstallSaveFile implements SaveFile {
    private final RegistryStore store;
    private final RegistryStore gameRegistry;

    public GameInstallSaveFile(RegistryStore store, RegistryStore gameRegistry) {
        this.store = store;
        this.gameRegistry = gameRegistry;
    }

    @Override
    public boolean applies() {
        return gameRegistry.pathExists("config/installdir") &&
                store.pathExists("savefiles/0/root") &&
                store.getObjectValueAsString("savefiles/0/root").getValue().equals("gameinstall");
    }

    @Override
    public UserFileSystemPath getMacInfo() {
        String root = computeRoot(OperatingSystems.OperatingSystem.MAC);
        String macSpecial = store.getObjectValueAsString("rootoverrides/0/addpath").getValue();
        String path = store.getObjectValueAsString("savefiles/3/path").getValue();
        return new UserFileSystemPath(root + "/" + macSpecial, path);
    }

    @Override
    public UserFileSystemPath getWindowsInfo() {
        String root = computeRoot(OperatingSystems.OperatingSystem.WINDOWS);
        String path = store.getObjectValueAsString("savefiles/3/path").getValue();
        return new UserFileSystemPath(root, path);
    }

    private String computeRoot(OperatingSystems.OperatingSystem os) {
        String installationDir = gameRegistry.getObjectValueAsString("config/installdir").getValue();
        return SteamInstallationPaths.get(os) + "/" + installationDir;
    }
}
