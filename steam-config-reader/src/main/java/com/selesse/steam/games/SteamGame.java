package com.selesse.steam.games;

public class SteamGame {
    private final SteamGameMetadata metadata;
    private final SteamGameConfig config;

    public SteamGame(SteamGameMetadata metadata, SteamGameConfig config) {
        this.metadata = metadata;
        this.config = config;
    }

    public String getName() {
        if (metadata.getName().isBlank()) {
            return config.getGameName();
        }
        return metadata.getName();
    }

    public boolean isInstalled() {
        return metadata.isInstalled();
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
