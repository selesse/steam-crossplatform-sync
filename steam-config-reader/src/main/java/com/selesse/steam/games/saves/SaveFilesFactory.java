package com.selesse.steam.games.saves;

import com.selesse.steam.registry.implementation.RegistryStore;

public class SaveFilesFactory {
    public static SaveFile determineSaveFile(RegistryStore gameRegistry) {
        GameInstallSaveFile gameInstallSaveFile = new GameInstallSaveFile(gameRegistry);
        if (gameInstallSaveFile.applies()) {
            return gameInstallSaveFile;
        }
        EverythingInSaveFilesWindowsOnly everythingInSaveFilesWindowsOnly = new EverythingInSaveFilesWindowsOnly(gameRegistry);
        if (everythingInSaveFilesWindowsOnly.applies()) {
            return everythingInSaveFilesWindowsOnly;
        }
        EverythingInSaveFiles everythingInSaveFiles = new EverythingInSaveFiles(gameRegistry);
        if (everythingInSaveFiles.applies()) {
            return everythingInSaveFiles;
        }
        SaveFilesRootOverrides saveFilesRootOverrides = new SaveFilesRootOverrides(gameRegistry);
        if (saveFilesRootOverrides.applies()) {
            return saveFilesRootOverrides;
        }
        OnlyWindowsSaveFile onlyWindowsSaveFile = new OnlyWindowsSaveFile(gameRegistry);
        if (onlyWindowsSaveFile.applies()) {
            return onlyWindowsSaveFile;
        }
        throw new UnableToParseSaveException(gameRegistry.getObjectValueAsString("common/name").getValue());
    }
}
