package com.selesse.steam.games.saves;

import static com.selesse.os.OperatingSystems.OperatingSystem.WINDOWS;

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
        if (!ufs.pathExists("savefiles")
                || steamApp.getSupportedOperatingSystems().size() > 1) {
            return false;
        }
        RegistryObject saveFiles = ufs.getObjectValueAsObject("savefiles");
        List<RegistryObject> objects = saveFiles.getKeys().stream()
                .map(saveFiles::getObjectValueAsObject)
                .toList();
        return objects.stream().noneMatch(x -> x.getKeys().contains("platforms"));
    }

    @Override
    public UserFileSystemPath getMacInfo() {
        return null;
    }

    @Override
    public UserFileSystemPath getWindowsInfo() {
        RegistryObject saveFiles = ufs.getObjectValueAsObject("savefiles");
        var saveFileObjects = saveFiles.getKeys().stream()
                .map(x -> new SaveFileObject(steamApp, saveFiles.getObjectValueAsObject(x)))
                .toList();
        SaveFileObject saveFileObject = saveFileObjects.stream()
                .filter(object -> object.hasRoot() && object.hasPath())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unable to parse savefile"));
        return new UserFileSystemPath(saveFileObject.getRoot(WINDOWS), saveFileObject.getPath());
    }

    @Override
    public List<UserFileSystemPath> getWindowsSavePaths() {
        RegistryObject saveFiles = ufs.getObjectValueAsObject("savefiles");
        var saveFileObjects = saveFiles.getKeys().stream()
                .map(x -> new SaveFileObject(steamApp, saveFiles.getObjectValueAsObject(x)))
                .toList();
        return saveFileObjects.stream()
                .filter(object -> object.hasRoot() && object.hasPath())
                .map(object -> UserFileSystemPath.fromSaveFile(object, WINDOWS))
                .toList();
    }

    @Override
    public UserFileSystemPath getLinuxInfo() {
        return null;
    }
}
