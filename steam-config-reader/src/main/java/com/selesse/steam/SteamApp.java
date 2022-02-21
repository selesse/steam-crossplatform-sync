package com.selesse.steam;

import com.google.common.base.Splitter;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.games.UserFileSystem;
import com.selesse.steam.registry.SteamOperatingSystem;
import com.selesse.steam.registry.implementation.RegistryStore;
import com.selesse.steam.registry.implementation.RegistryString;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<OperatingSystems.OperatingSystem> getSupportedOperatingSystems() {
        if (!getRegistryStore().pathExists("common/oslist")) {
            return List.of(OperatingSystems.OperatingSystem.WINDOWS);
        }
        RegistryString objectValueAsString = registryStore.getObjectValueAsString("common/oslist");
        List<String> oses = Splitter.on(",").splitToList(objectValueAsString.getValue());
        return oses.stream().map(x -> SteamOperatingSystem.fromString(x).toOperatingSystem()).collect(Collectors.toList());
    }

    public String getMacInstallationPath() {
        return new UserFileSystem(this).getMacInstallationPath();
    }

    public String getWindowsInstallationPath() {
        return new UserFileSystem(this).getWindowsInstallationPath();
    }

    public String getLinuxInstallationPath() {
        return new UserFileSystem(this).getLinuxInstallationPath();
    }

    public boolean hasUserFileSystem() {
        return registryStore.getObjectValueAsObject("ufs") != null && registryStore.pathExists("ufs/savefiles");
    }
}
