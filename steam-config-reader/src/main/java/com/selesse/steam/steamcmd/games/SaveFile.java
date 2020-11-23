package com.selesse.steam.steamcmd.games;

import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryStore;

public abstract class SaveFile {
    protected final RegistryStore gameRegistry;
    protected final RegistryObject ufs;

    public SaveFile(RegistryStore gameRegistry) {
        this.gameRegistry = gameRegistry;
        this.ufs = gameRegistry.getObjectValueAsObject("ufs");
    }

    abstract boolean applies();
    abstract UserFileSystemPath getMacInfo();
    abstract UserFileSystemPath getWindowsInfo();
}
