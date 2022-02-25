package com.selesse.steam.games;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.AppType;
import com.selesse.steam.SteamApp;
import com.selesse.steam.registry.implementation.RegistryStore;
import java.util.List;
import java.util.stream.Stream;

public class SteamGame {
    private final SteamGameMetadata metadata;
    private final SteamApp app;

    public SteamGame(SteamGameMetadata metadata, RegistryStore config) {
        this.metadata = metadata;
        this.app = new SteamApp(config);
    }

    public String getName() {
        return metadata.name();
    }

    public RegistryStore getRegistryStore() {
        return app.getRegistryStore();
    }

    public boolean isInstalled() {
        return metadata.installed();
    }

    public AppType getAppType() {
        return app.getType();
    }

    public long getId() {
        return metadata.gameId();
    }

    public List<OperatingSystems.OperatingSystem> supportedOperatingSystems() {
        return app.getSupportedOperatingSystems();
    }

    public boolean hasUserCloud() {
        return app.hasUserFileSystem();
    }

    public String getInstallationPath(OperatingSystems.OperatingSystem operatingSystem) {
        return switch (operatingSystem) {
            case WINDOWS -> getWindowsInstallationPath();
            case MAC -> getMacInstallationPath();
            case LINUX -> getLinuxInstallationPath();
        };
    }

    public String getWindowsInstallationPath() {
        return app.getWindowsInstallationPath();
    }

    public String getMacInstallationPath() {
        return app.getMacInstallationPath();
    }

    public String getLinuxInstallationPath() {
        return app.getLinuxInstallationPath();
    }

    public boolean isGame() {
        return getAppType() == AppType.GAME;
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", getName(), getId());
    }

    public String metadata() {
        return metadata.toString();
    }

    public boolean hasMacPath() {
        try {
            return getMacInstallationPath() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean hasLinuxPath() {
        try {
            return getLinuxInstallationPath() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean hasWindowsPath() {
        try {
            return getWindowsInstallationPath() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean hasComputedInstallationPath() {
        return Stream.of(hasWindowsPath(), hasMacPath(), hasLinuxPath()).anyMatch(x -> x);
    }
}
