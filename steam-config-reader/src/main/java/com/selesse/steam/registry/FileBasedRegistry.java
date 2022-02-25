package com.selesse.steam.registry;

import com.google.common.collect.Lists;
import com.selesse.files.RuntimeExceptionFiles;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryParser;
import com.selesse.steam.registry.implementation.RegistryStore;
import com.selesse.steam.registry.implementation.RegistryString;
import java.nio.file.Path;
import java.util.List;

public abstract class FileBasedRegistry extends SteamRegistry {
    private final RegistryStore registryStore;

    abstract Path registryPath();

    public FileBasedRegistry() {
        ensureRegistryPathExists();
        this.registryStore = RegistryParser.parse(RuntimeExceptionFiles.readAllLines(registryPath()));
    }

    private void ensureRegistryPathExists() {
        if (!registryPath().toFile().isFile()) {
            throw new RuntimeException("Registry file does not exist in " + registryPath());
        }
    }

    public long getCurrentlyRunningAppId() {
        RegistryString value = registryStore.getObjectValueAsString("Registry/HKCU/Software/Valve/Steam/RunningAppID");
        return Long.parseLong(value.getValue());
    }

    public List<Long> getInstalledAppIds() {
        RegistryObject object = getAppsRegistryObject();
        if (object == null) {
            return Lists.newArrayList();
        }
        return object.getKeys().stream()
                .map(Long::valueOf)
                .filter(this::isInstalled)
                .sorted()
                .toList();
    }

    private RegistryObject getAppsRegistryObject() {
        return registryStore.getObjectValueAsObject("Registry/HKCU/Software/Valve/Steam/apps");
    }

    private RegistryObject getRegistryObject(long gameId) {
        return getAppsRegistryObject().getObjectValueAsObject("" + gameId);
    }

    private boolean isInstalled(Long gameId) {
        RegistryObject registryObject = getRegistryObject(gameId);
        return registryObject.pathExists("installed")
                && registryObject.getObjectValueAsString("installed").getValue().equals("1");
    }
}
