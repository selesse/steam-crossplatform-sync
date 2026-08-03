package com.selesse.steam.games;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.AppType;
import com.selesse.steam.SteamApp;
import com.selesse.steam.registry.implementation.RegistryObject;
import java.util.List;
import java.util.stream.Stream;

public class SteamGame {
    private final SteamGameMetadata metadata;
    private final SteamApp app;

    public SteamGame(RegistryObject config) {
        this.app = new SteamApp(config);
        this.metadata = new SteamGameMetadata(getId(), getName());
    }

    public String getName() {
        return app.getName();
    }

    public RegistryObject getRegistryObject() {
        return app.getRegistryObject();
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

    public List<UserFileSystemPath> getSavePaths(OperatingSystems.OperatingSystem os) {
        return app.getSavePaths(os);
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

    /** Whether {@link #getSavePaths} resolves to anything for {@code os}. */
    public boolean hasSavePathsFor(OperatingSystems.OperatingSystem os) {
        try {
            return !getSavePaths(os).isEmpty();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean hasAnySavePaths() {
        return Stream.of(
                        OperatingSystems.OperatingSystem.WINDOWS,
                        OperatingSystems.OperatingSystem.MAC,
                        OperatingSystems.OperatingSystem.LINUX)
                .anyMatch(this::hasSavePathsFor);
    }
}
