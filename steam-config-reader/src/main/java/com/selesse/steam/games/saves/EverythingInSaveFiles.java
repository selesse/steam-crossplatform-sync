package com.selesse.steam.games.saves;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.SteamApp;
import com.selesse.steam.games.UserFileSystemPath;
import com.selesse.steam.games.UserFileSystemPathConverter;
import java.util.List;

public class EverythingInSaveFiles extends SaveFile {
    public EverythingInSaveFiles(SteamApp steamApp) {
        super(steamApp);
    }

    @Override
    public boolean applies() {
        return ufs.pathExists("savefiles")
                && ufs.getObjectValueAsObject("savefiles").getKeys().size() > 1;
    }

    @Override
    public UserFileSystemPath getWindowsInfo() {
        return getWindowsSavePaths().get(0);
    }

    @Override
    public UserFileSystemPath getMacInfo() {
        return getMacSavePaths().get(0);
    }

    @Override
    public UserFileSystemPath getLinuxInfo() {
        return getLinuxSavePaths().get(0);
    }

    @Override
    public List<UserFileSystemPath> getWindowsSavePaths() {
        return getSaveFiles(OperatingSystems.OperatingSystem.WINDOWS);
    }

    @Override
    public List<UserFileSystemPath> getMacSavePaths() {
        return getSaveFiles(OperatingSystems.OperatingSystem.MAC);
    }

    @Override
    public List<UserFileSystemPath> getLinuxSavePaths() {
        return getSaveFiles(OperatingSystems.OperatingSystem.LINUX);
    }

    private List<UserFileSystemPath> getSaveFiles(OperatingSystems.OperatingSystem os) {
        var saveFileObjects = ufs.getObjectValueAsObject("savefiles").getKeys().stream()
                .map(key -> ufs.getObjectValueAsObject("savefiles/" + key))
                .map(registryObject -> new SaveFileObject(steamApp, registryObject))
                .map(saveFileObject -> UserFileSystemPath.fromSaveFile(saveFileObject, os))
                .toList();
        if (ufs.pathExists("rootoverrides") && os != OperatingSystems.OperatingSystem.WINDOWS) {
            var overrides = ufs.getObjectValueAsObject("rootoverrides").getKeys().stream()
                    .map(key -> ufs.getObjectValueAsObject("rootoverrides/" + key))
                    .map(RootOverrideObject::new)
                    .toList();
            if (os == OperatingSystems.OperatingSystem.LINUX
                    && overrides.stream().noneMatch(x -> x.getOs() == OperatingSystems.OperatingSystem.LINUX)) {
                overrides = UserFileSystemPathConverter.convertMacToLinux(overrides);
            }
            return overrides.stream()
                    .filter(o -> o.getOs() == os)
                    .map(overrideObject ->
                            UserFileSystemPathConverter.convert(saveFileObjects, overrideObject, steamApp))
                    .flatMap(List::stream)
                    .toList();
        }
        if (isNonWindowsButWeOnlyHaveWindowsSaveFiles(os, saveFileObjects)) {
            return saveFileObjects.stream().map(x -> x.convert(os)).toList();
        }
        if (saveFileObjects.stream().anyMatch(x -> x.getPlatform() != null)) {
            return saveFileObjects.stream().filter(x -> x.getPlatform() == os).toList();
        }
        return saveFileObjects;
    }

    private boolean isNonWindowsButWeOnlyHaveWindowsSaveFiles(
            OperatingSystems.OperatingSystem os, List<UserFileSystemPath> saveFileObjects) {
        return steamApp.getSupportedOperatingSystems().size() > 1
                && onlyHasWindows(saveFileObjects)
                && os != OperatingSystems.OperatingSystem.WINDOWS;
    }

    private boolean onlyHasWindows(List<UserFileSystemPath> saveFileObjects) {
        return saveFileObjects.stream().allMatch(x -> x.getPlatform() == OperatingSystems.OperatingSystem.WINDOWS);
    }
}
