package com.selesse.steam.games.saves;

import com.selesse.steam.SteamApp;
import com.selesse.steam.games.UserFileSystemPath;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryStore;

public abstract class SaveFile {
    protected final SteamApp steamApp;
    protected final RegistryStore gameRegistry;
    protected final RegistryObject ufs;

    public SaveFile(SteamApp steamApp) {
        this.steamApp = steamApp;
        this.gameRegistry = steamApp.getRegistryStore();
        this.ufs = gameRegistry.getObjectValueAsObject("ufs");
    }

    public abstract boolean applies();

    public abstract UserFileSystemPath getMacInfo();

    public abstract UserFileSystemPath getWindowsInfo();

    public abstract UserFileSystemPath getLinuxInfo();
}
