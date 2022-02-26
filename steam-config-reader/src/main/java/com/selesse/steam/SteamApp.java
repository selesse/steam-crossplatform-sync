package com.selesse.steam;

import com.google.common.base.Splitter;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.games.SteamInstallationPaths;
import com.selesse.steam.games.UserFileSystem;
import com.selesse.steam.games.UserFileSystemPath;
import com.selesse.steam.registry.SteamOperatingSystem;
import com.selesse.steam.registry.implementation.RegistryStore;
import com.selesse.steam.registry.implementation.RegistryString;
import java.util.List;

public class SteamApp {
    private final RegistryStore registryStore;

    public SteamApp(RegistryStore registryStore) {
        this.registryStore = registryStore;
    }

    public RegistryStore getRegistryStore() {
        return registryStore;
    }

    public String getName() {
        RegistryString objectValueAsString = registryStore.getObjectValueAsString("common/name");
        return objectValueAsString.getValue();
    }

    public AppType getType() {
        RegistryString objectValueAsString = registryStore.getObjectValueAsString("common/type");
        return AppType.fromString(objectValueAsString);
    }

    public String getInstallationDirectory() {
        return registryStore.getObjectValueAsString("config/installdir").getValue();
    }

    public String getInstallationDirectory(OperatingSystems.OperatingSystem os) {
        return SteamInstallationPaths.get(os) + "/" + getInstallationDirectory();
    }

    public List<OperatingSystems.OperatingSystem> getSupportedOperatingSystems() {
        if (!getRegistryStore().pathExists("common/oslist")) {
            return List.of(OperatingSystems.OperatingSystem.WINDOWS);
        }
        RegistryString objectValueAsString = registryStore.getObjectValueAsString("common/oslist");
        List<String> oses = Splitter.on(",").splitToList(objectValueAsString.getValue());
        return oses.stream()
                .map(x -> SteamOperatingSystem.fromString(x).toOperatingSystem())
                .toList();
    }

    public String getWindowsInstallationPath() {
        return new UserFileSystem(this).getWindowsInstallationPath();
    }

    public List<UserFileSystemPath> getWindowsInstallationPaths() {
        return new UserFileSystem(this).getWindowsInstallationPaths();
    }

    public String getMacInstallationPath() {
        return new UserFileSystem(this).getMacInstallationPath();
    }

    public List<UserFileSystemPath> getMacInstallationPaths() {
        return new UserFileSystem(this).getMacInstallationPaths();
    }

    public String getLinuxInstallationPath() {
        return new UserFileSystem(this).getLinuxInstallationPath();
    }

    public List<UserFileSystemPath> getLinuxInstallationPaths() {
        return new UserFileSystem(this).getLinuxInstallationPaths();
    }

    public boolean hasUserFileSystem() {
        return registryStore.getObjectValueAsObject("ufs") != null && registryStore.pathExists("ufs/savefiles");
    }
}
