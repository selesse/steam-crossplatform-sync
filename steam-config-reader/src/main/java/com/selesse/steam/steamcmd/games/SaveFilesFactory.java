package com.selesse.steam.steamcmd.games;

import com.selesse.steam.registry.implementation.RegistryStore;

public class SaveFilesFactory {
    public static SaveFile determineSaveFile(RegistryStore store) {
        GameInstallSaveFile gameInstallSaveFile = new GameInstallSaveFile(store);
        if (gameInstallSaveFile.applies()) {
            return gameInstallSaveFile;
        }
        EverythingInSaveFiles everythingInSaveFiles = new EverythingInSaveFiles(store);
        if (everythingInSaveFiles.applies()) {
            return everythingInSaveFiles;
        }
        SaveFilesRootOverrides saveFilesRootOverrides = new SaveFilesRootOverrides(store);
        if (saveFilesRootOverrides.applies()) {
            return saveFilesRootOverrides;
        }
        return new UnknownSaveFormat(store);
    }
}
