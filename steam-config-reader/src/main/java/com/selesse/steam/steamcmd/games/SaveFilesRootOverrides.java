package com.selesse.steam.steamcmd.games;

import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryStore;
import com.selesse.steam.registry.implementation.RegistryString;

public class SaveFilesRootOverrides extends SaveFile {
    public SaveFilesRootOverrides(RegistryStore gameRegistry) {
        super(gameRegistry);
    }

    @Override
    public boolean applies() {
        return ufs.pathExists("savefiles") &&
                ufs.pathExists("rootoverrides") &&
                ufs.getObjectValueAsObject("savefiles").getKeys().size() == 1 &&
                ufs.getObjectValueAsObject("rootoverrides").getKeys().size() >= 1;
    }

    @Override
    public UserFileSystemPath getMacInfo() {
        RegistryObject rootOverrides = ufs.getObjectValueAsObject("rootoverrides");
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
        String path = getRawWindowsInfo().getRawPath();
        RegistryString addpath = macConfig.getObjectValueAsString("addpath");
        if (addpath != null) {
            String append = addpath.getValue();
            root = root + append;
        }
        RegistryObject transforms = macConfig.getObjectValueAsObject("pathtransforms/0");
        if (transforms != null) {
            String find = transforms.getObjectValueAsString("find").getValue();
            String replace = transforms.getObjectValueAsString("replace").getValue();

            path = path.replace(find, replace);
        }
        return new UserFileSystemPath(root, computePath(path));
    }

    @Override
    public UserFileSystemPath getWindowsInfo() {
        UserFileSystemPath rawWindowsInfo = getRawWindowsInfo();
        return new UserFileSystemPath(rawWindowsInfo.getRoot(), computePath(rawWindowsInfo.getRawPath()));
    }

    private UserFileSystemPath getRawWindowsInfo() {
        RegistryObject objectValueAsObject = ufs.getObjectValueAsObject("savefiles/0");
        String root = objectValueAsObject.getObjectValueAsString("root").getValue();
        String path = objectValueAsObject.getObjectValueAsString("path").getValue();
        return new UserFileSystemPath(root, path);
    }

    private String computePath(String path) {
        if (gameRegistry.getObjectValueAsString("common/name").getValue().equals("Oxygen Not Included")) {
            return path.replace("cloud_save_files", "save_files");
        }
        return path;
    }
}
