package com.selesse.steam.games;

import com.selesse.steam.AppType;
import com.selesse.steam.SteamApp;
import com.selesse.steam.registry.implementation.RegistryStore;

public class SteamGame {
    private final SteamGameMetadata metadata;
    private final SteamGameConfig config;
    private final SteamApp app;

    public SteamGame(SteamGameMetadata metadata, RegistryStore config) {
        this.metadata = metadata;
        this.app = new SteamApp(config);
        this.config = new SteamGameConfig(config);
    }

    public String getName() {
        if (metadata.getName().isBlank()) {
            return config.getGameName();
        }
        return metadata.getName();
    }

    public RegistryStore getRegistryStore() {
        return app.getRegistryStore();
    }

    public boolean isInstalled() {
        return metadata.isInstalled();
    }

    public AppType getAppType() {
        return app.getType();
    }

    public long getId() {
        return metadata.getGameId();
    }

    public boolean hasUserCloud() {
        return config.hasUserFileSystem();
    }

    public String getWindowsInstallationPath() {
        return config.getWindowsInstallationPath();
    }

    public String getMacInstallationPath() {
        return config.getMacInstallationPath();
    }

    public boolean isGame() {
        return config.loaded() && config.isGame();
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", getName(), getId());
    }

    public String metadata() {
        return metadata.toString();
    }

    public boolean hasComputedInstallationPath() {
        boolean windows, mac;
        try {
            windows = getWindowsInstallationPath() != null;
            mac = getMacInstallationPath() != null;
        } catch (RuntimeException e) {
            return false;
        }
        return windows && mac;
    }
}
