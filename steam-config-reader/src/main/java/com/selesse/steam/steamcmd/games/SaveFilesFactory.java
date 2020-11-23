package com.selesse.steam.steamcmd.games;

import com.selesse.steam.registry.implementation.RegistryStore;

public class SaveFilesFactory {
    public static SaveFile determineSaveFile(RegistryStore ufsStore, RegistryStore gameRegistry) {
        GameInstallSaveFile gameInstallSaveFile = new GameInstallSaveFile(ufsStore, gameRegistry);
        if (gameInstallSaveFile.applies()) {
            return gameInstallSaveFile;
        }
        EverythingInSaveFiles everythingInSaveFiles = new EverythingInSaveFiles(ufsStore);
        if (everythingInSaveFiles.applies()) {
            return everythingInSaveFiles;
        }
        SaveFilesRootOverrides saveFilesRootOverrides = new SaveFilesRootOverrides(ufsStore, gameRegistry);
        if (saveFilesRootOverrides.applies()) {
            return saveFilesRootOverrides;
        }
        throw new UnableToParseSaveException(gameRegistry.getObjectValueAsString("common/name").getValue());
    }
}
