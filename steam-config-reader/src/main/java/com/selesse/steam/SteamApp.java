package com.selesse.steam;

import com.google.common.base.Splitter;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.games.SteamInstallationPaths;
import com.selesse.steam.games.UserFileSystem;
import com.selesse.steam.games.UserFileSystemPath;
import com.selesse.steam.registry.SteamOperatingSystem;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryString;
import java.util.List;

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
        return new UserFileSystem(this).getSavePaths(os);
    }

    public boolean hasUserFileSystem() {
        return registryObject.getObjectValueAsObject("ufs") != null && registryObject.pathExists("ufs/savefiles");
    }
}
