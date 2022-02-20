package com.selesse.steam.games.saves;

import com.selesse.steam.games.UserFileSystemPath;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryStore;

import java.util.List;
import java.util.stream.Collectors;

public class EverythingInSaveFilesWindowsOnly extends SaveFile {
    public EverythingInSaveFilesWindowsOnly(RegistryStore gameRegistry) {
        super(gameRegistry);
    }

    @Override
    public boolean applies() {
        if (!ufs.pathExists("savefiles")) {
            return false;
        }
        RegistryObject saveFiles = ufs.getObjectValueAsObject("savefiles");
        List<RegistryObject> objects =
                saveFiles.getKeys().stream().map(saveFiles::getObjectValueAsObject).collect(Collectors.toList());
        return objects.stream().noneMatch(x -> x.getKeys().contains("platforms"));
    }

    @Override
    public UserFileSystemPath getMacInfo() {
        return null;
    }

    @Override
    public UserFileSystemPath getWindowsInfo() {
        RegistryObject object = ufs.getObjectValueAsObject("savefiles/1");
        String root = object.getObjectValueAsString("root").getValue();
        String path = object.getObjectValueAsString("path").getValue();
        return new UserFileSystemPath(root, path);
    }

    @Override
    public UserFileSystemPath getLinuxInfo() {
        return null;
    }
}
