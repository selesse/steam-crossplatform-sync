package com.selesse.steam.steamcmd.games;

import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryStore;
import com.selesse.steam.registry.implementation.RegistryString;
import com.selesse.steam.registry.implementation.RegistryValue;

public class EverythingInSaveFiles implements SaveFile {
    private final RegistryStore registry;

    public EverythingInSaveFiles(RegistryStore userFileSystemRegistry) {
        this.registry = userFileSystemRegistry;
    }

    @Override
    public boolean applies() {
        return registry.pathExists("savefiles") &&
                registry.getObjectValueAsObject("savefiles").getKeys().size() > 1;
    }

    @Override
    public UserFileSystemPath getMacInfo() {
        RegistryObject object = getSaveFile("MacOS");
        String root = object.getObjectValueAsString("root").getValue();
        String path = object.getObjectValueAsString("path").getValue();
        return new UserFileSystemPath(root, path);
    }

    @Override
    public UserFileSystemPath getWindowsInfo() {
        RegistryObject object = getSaveFile("Windows");
        String root = object.getObjectValueAsString("root").getValue();
        String path = object.getObjectValueAsString("path").getValue();
        return new UserFileSystemPath(root, path);
    }

    private RegistryObject getSaveFile(String platform) {
        RegistryObject saveFiles = registry.getObjectValueAsObject("savefiles");
        for (String saveFile : saveFiles.getKeys()) {
            RegistryValue registryValue = saveFiles.get(saveFile);
            if (registryValue instanceof RegistryObject) {
                RegistryObject platforms = (RegistryObject) ((RegistryObject) registryValue).get("platforms");
                RegistryString registryString = (RegistryString) platforms.get("1");
                if (registryString.getValue().equals(platform)) {
                    return (RegistryObject) saveFiles.get(saveFile);
                }
            }
        }
        throw new IllegalArgumentException("Could not find " + platform + " in config");
    }
}