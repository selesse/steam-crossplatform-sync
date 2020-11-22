package com.selesse.steam.steamcmd.games;

import com.selesse.steam.registry.implementation.RegistryStore;

public class UnknownSaveFormat implements SaveFile {
    public UnknownSaveFormat(RegistryStore store) {
    }

    @Override
    public boolean applies() {
        return false;
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
