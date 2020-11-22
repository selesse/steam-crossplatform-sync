package com.selesse.steam.steamcmd.games;

import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryStore;
import com.selesse.steam.registry.implementation.RegistryString;

public class SaveFilesRootOverrides implements SaveFile {
    private final RegistryStore store;

    public SaveFilesRootOverrides(RegistryStore store) {
        this.store = store;
    }

    @Override
    public boolean applies() {
        return store.getObjectValueAsObject("savefiles").getKeys().size() == 1 &&
                store.getObjectValueAsObject("rootoverrides").getKeys().size() >= 1;
    }

    @Override
    public UserFileSystemPath getMacInfo() {
        RegistryObject rootOverrides = store.getObjectValueAsObject("rootoverrides");
        RegistryObject macConfig = null;
        for (String objectKeys : rootOverrides.getKeys()) {
            RegistryObject nonWindowsConfig = rootOverrides.getObjectValueAsObject(objectKeys);
            if (nonWindowsConfig.getObjectValueAsString("os").getValue().equals("MacOS")) {
                macConfig = nonWindowsConfig;
            }
        }
        if (macConfig == null) {
            throw new IllegalArgumentException("Could not find Mac config");
        }
        String root = macConfig.getObjectValueAsString("useinstead").getValue();
        RegistryString addpath = macConfig.getObjectValueAsString("addpath");
        if (addpath != null) {
            String append = addpath.getValue();
            root = root + append;
        }
        return new UserFileSystemPath(root, getWindowsInfo().getPath());
    }

    @Override
    public UserFileSystemPath getWindowsInfo() {
        RegistryObject objectValueAsObject = store.getObjectValueAsObject("savefiles/0");
        String root = objectValueAsObject.getObjectValueAsString("root").getValue();
        String path = objectValueAsObject.getObjectValueAsString("path").getValue();
        return new UserFileSystemPath(root, path);
    }
}
