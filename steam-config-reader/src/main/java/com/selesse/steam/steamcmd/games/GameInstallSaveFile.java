package com.selesse.steam.steamcmd.games;

import com.selesse.steam.registry.implementation.RegistryStore;

public class GameInstallSaveFile implements SaveFile {
    private final RegistryStore store;

    public GameInstallSaveFile(RegistryStore store) {
        this.store = store;
    }

    @Override
    public boolean applies() {
        return store.getObjectValueAsString("savefiles/0/root").getValue().equals("gameinstall");
    }

    @Override
    public UserFileSystemPath getMacInfo() {
        return new UserFileSystemPath("", "");
    }

    @Override
    public UserFileSystemPath getWindowsInfo() {
        return new UserFileSystemPath("", "");
    }
}
