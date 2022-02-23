package com.selesse.steam.games.saves;

import com.selesse.steam.SteamApp;
import com.selesse.steam.games.UserFileSystemPath;
import com.selesse.steam.registry.implementation.RegistryObject;

import java.util.List;

public class EverythingInSaveFilesWindowsOnly extends SaveFile {
    public EverythingInSaveFilesWindowsOnly(SteamApp steamApp) {
        super(steamApp);
    }

    @Override
    public boolean applies() {
        if (!ufs.pathExists("savefiles") || steamApp.getSupportedOperatingSystems().size() > 1) {
            return false;
        }
        RegistryObject saveFiles = ufs.getObjectValueAsObject("savefiles");
        List<RegistryObject> objects =
                saveFiles.getKeys().stream().map(saveFiles::getObjectValueAsObject).toList();
        return objects.stream().noneMatch(x -> x.getKeys().contains("platforms"));
    }

    @Override
    public UserFileSystemPath getMacInfo() {
        return null;
    }

    @Override
    public UserFileSystemPath getWindowsInfo() {
        RegistryObject object;
        if (ufs.pathExists("savefiles/0")) {
            object = ufs.getObjectValueAsObject("savefiles/0");
        } else if (ufs.pathExists("savefiles/1")) {
            object = ufs.getObjectValueAsObject("savefiles/1");
        } else {
            throw new IllegalArgumentException("Did not know how to parse savefiles");
        }
        String root = object.getObjectValueAsString("root").getValue();
        String path = object.getObjectValueAsString("path").getValue();
        return new UserFileSystemPath(root, path);
    }

    @Override
    public UserFileSystemPath getLinuxInfo() {
        return null;
    }
}
