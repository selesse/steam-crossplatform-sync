package com.selesse.steam.games.saves;

import com.selesse.steam.games.UserFileSystemPath;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryStore;

public abstract class SaveFile {
    protected final RegistryStore gameRegistry;
    protected final RegistryObject ufs;

    public SaveFile(RegistryStore gameRegistry) {
        this.gameRegistry = gameRegistry;
        this.ufs = gameRegistry.getObjectValueAsObject("ufs");
    }

    public abstract boolean applies();
    public abstract UserFileSystemPath getMacInfo();
    public abstract UserFileSystemPath getWindowsInfo();
}
