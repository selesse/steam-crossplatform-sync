package com.selesse.steam.steamcmd.games;

import com.selesse.steam.registry.implementation.RegistryStore;

public class SteamGameConfig {
    private final RegistryStore registryStore;

    public SteamGameConfig(RegistryStore registryStore) {
        this.registryStore = registryStore;
    }

    public String getMacInstallationPath() {
        return new UserFileSystem(getUserFileSystemRegistry()).getMacInstallationPath();
    }

    public String getWindowsInstallationPath() {
        return new UserFileSystem(getUserFileSystemRegistry()).getWindowsInstallationPath();
    }

    public boolean hasUserFileSystem() {
        return registryStore.getObjectValueAsObject("ufs") != null;
    }

    private RegistryStore getUserFileSystemRegistry() {
        return new RegistryStore(registryStore.getObjectValueAsObject("ufs"));
    }
}
