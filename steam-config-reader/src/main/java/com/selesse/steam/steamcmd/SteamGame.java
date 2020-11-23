package com.selesse.steam.steamcmd;

import com.selesse.steam.steamcmd.games.SteamGameConfig;
import com.selesse.steam.steamcmd.games.SteamGameMetadata;

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
}
