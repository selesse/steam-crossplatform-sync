package com.selesse.steam;

import com.google.common.base.Splitter;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.games.SteamInstallationPaths;
import com.selesse.steam.games.UserFileSystemPath;
import com.selesse.steam.games.saves.SaveFilesFactory;
import com.selesse.steam.registry.SteamOperatingSystem;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryString;
import java.util.List;
import java.util.stream.Stream;

public class SteamApp {
    private final RegistryObject registryObject;

    public SteamApp(RegistryObject registryObject) {
        this.registryObject = registryObject;
    }

    public RegistryObject getRegistryObject() {
        return registryObject;
    }

    public long getId() {
        RegistryString objectValueAsString = registryObject.getObjectValueAsString("common/gameid");
        return Long.parseLong(objectValueAsString.getValue());
    }

    public String getName() {
        RegistryString objectValueAsString = registryObject.getObjectValueAsString("common/name");
        return objectValueAsString.getValue();
    }

    public AppType getType() {
        RegistryString objectValueAsString = registryObject.getObjectValueAsString("common/type");
        return AppType.fromString(objectValueAsString);
    }

    public String getInstallationDirectory() {
        return registryObject.getObjectValueAsString("config/installdir").getValue();
    }

    public String getInstallationDirectory(OperatingSystems.OperatingSystem os) {
        return SteamInstallationPaths.get(os) + "/" + getInstallationDirectory();
    }

    public List<OperatingSystems.OperatingSystem> getSupportedOperatingSystems() {
        if (!getRegistryObject().pathExists("common/oslist")) {
            return List.of(OperatingSystems.OperatingSystem.WINDOWS);
        }
        RegistryString objectValueAsString = registryObject.getObjectValueAsString("common/oslist");
        List<String> oses = Splitter.on(",").splitToList(objectValueAsString.getValue());
        return oses.stream()
                .flatMap(x -> SteamOperatingSystem.tryFromString(x).stream())
                .map(SteamOperatingSystem::toOperatingSystem)
                .toList();
    }

    public boolean supports(OperatingSystems.OperatingSystem operatingSystem) {
        return getSupportedOperatingSystems().contains(operatingSystem);
    }

    /**
     * Where this app's saves live when running on {@code os}, or empty if it has none there.
     */
    public List<UserFileSystemPath> getSavePaths(OperatingSystems.OperatingSystem os) {
        // ufs entries and rootoverrides only ever describe windows/macos/linux, so SteamOS reads
        // as Linux from here down. Windows is never gated on declared support: an app with no
        // oslist is treated as Windows-only, and Windows-rooted ufs entries are the fallback
        // shape even for apps that don't list Windows.
        OperatingSystems.OperatingSystem target =
                os == OperatingSystems.OperatingSystem.STEAM_OS ? OperatingSystems.OperatingSystem.LINUX : os;
        if (target != OperatingSystems.OperatingSystem.WINDOWS && !supports(target)) {
            return List.of();
        }
        return SaveFilesFactory.determineSaveFile(this).savePathsFor(target);
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

    public boolean hasUserFileSystem() {
        return registryObject.getObjectValueAsObject("ufs") != null && registryObject.pathExists("ufs/savefiles");
    }

    public boolean isGame() {
        return getType() == AppType.GAME;
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", getName(), getId());
    }
}
