package com.selesse.steam.games;

import com.selesse.steam.registry.implementation.RegistryStore;

public class SteamGameConfig {
    private final RegistryStore registryStore;

    public SteamGameConfig(RegistryStore registryStore) {
        this.registryStore = registryStore;
    }

    public String getGameName() {
        return registryStore.getObjectValueAsString("common/name").getValue();
    }

    public String getMacInstallationPath() {
        return new UserFileSystem(registryStore).getMacInstallationPath();
    }

    public String getWindowsInstallationPath() {
        return new UserFileSystem(registryStore).getWindowsInstallationPath();
    }

    public boolean hasUserFileSystem() {
        return registryStore.getObjectValueAsObject("ufs") != null && registryStore.pathExists("ufs/savefiles");
    }

    private RegistryStore getUserFileSystemRegistry() {
        return new RegistryStore(registryStore.getObjectValueAsObject("ufs"));
    }

    public boolean loaded() {
        return registryStore != null;
    }

    public boolean isGame() {
        return registryStore.getObjectValueAsString("common/type").getValue().equals("Game");
    }
}
