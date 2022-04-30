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

    public SteamGame(RegistryStore config) {
        this.app = new SteamApp(config);
        this.metadata = new SteamGameMetadata(getId(), getName());
    }

    public String getName() {
        return app.getName();
    }

    public RegistryStore getRegistryStore() {
        return app.getRegistryStore();
    }

    public AppType getAppType() {
        return app.getType();
    }

    public long getId() {
        return app.getId();
    }

    public List<OperatingSystems.OperatingSystem> supportedOperatingSystems() {
        return app.getSupportedOperatingSystems();
    }

    public boolean hasUserCloud() {
        return app.hasUserFileSystem();
    }

    public List<UserFileSystemPath> getInstallationPaths(OperatingSystems.OperatingSystem operatingSystem) {
        return switch (operatingSystem) {
            case WINDOWS -> getWindowsInstallationPaths();
            case MAC -> getMacInstallationPaths();
            case LINUX, STEAM_OS -> getLinuxInstallationPaths();
        };
    }

    public List<UserFileSystemPath> getWindowsInstallationPaths() {
        return app.getWindowsInstallationPaths();
    }

    public List<UserFileSystemPath> getMacInstallationPaths() {
        return app.getMacInstallationPaths();
    }

    public List<UserFileSystemPath> getLinuxInstallationPaths() {
        return app.getLinuxInstallationPaths();
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

    public boolean hasWindowsPath() {
        try {
            return getWindowsInstallationPaths() != null
                    && !getWindowsInstallationPaths().isEmpty();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean hasMacPath() {
        try {
            return getMacInstallationPaths() != null
                    && !getMacInstallationPaths().isEmpty();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean hasLinuxPath() {
        try {
            return getLinuxInstallationPaths() != null
                    && !getLinuxInstallationPaths().isEmpty();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean hasComputedInstallationPath() {
        return Stream.of(hasWindowsPath(), hasMacPath(), hasLinuxPath()).anyMatch(x -> x);
    }
}
