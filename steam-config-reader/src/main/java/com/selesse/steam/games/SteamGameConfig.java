package com.selesse.steam.games;

import com.selesse.steam.SteamApp;
import com.selesse.steam.registry.implementation.RegistryStore;

public class SteamGameConfig {
    private final RegistryStore registryStore;
    private final SteamApp steamApp;

    public SteamGameConfig(SteamApp steamApp) {
        this.steamApp = steamApp;
        this.registryStore = steamApp.getRegistryStore();
    }

    public String getGameName() {
        return registryStore.getObjectValueAsString("common/name").getValue();
    }

    public String getMacInstallationPath() {
        return new UserFileSystem(steamApp).getMacInstallationPath();
    }

    public String getWindowsInstallationPath() {
        return new UserFileSystem(steamApp).getWindowsInstallationPath();
    }

    public String getLinuxInstallationPath() {
        return new UserFileSystem(steamApp).getLinuxInstallationPath();
    }

    public boolean hasUserFileSystem() {
        return registryStore.getObjectValueAsObject("ufs") != null && registryStore.pathExists("ufs/savefiles");
    }

    public boolean isGame() {
        return registryStore.getObjectValueAsString("common/type").getValue().equals("Game");
    }
}
