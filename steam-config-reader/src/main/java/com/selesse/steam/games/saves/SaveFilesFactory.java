package com.selesse.steam.games.saves;

import com.selesse.steam.SteamApp;

public class SaveFilesFactory {
    public static SaveFile determineSaveFile(SteamApp steamApp) {
        BypassRegistrySaveFile bypassRegistrySaveFile = new BypassRegistrySaveFile(steamApp);
        if (bypassRegistrySaveFile.applies()) {
            return bypassRegistrySaveFile;
        }
        GameInstallSaveFile gameInstallSaveFile = new GameInstallSaveFile(steamApp);
        if (gameInstallSaveFile.applies()) {
            return gameInstallSaveFile;
        }
        EverythingInSaveFilesWindowsOnly everythingInSaveFilesWindowsOnly = new EverythingInSaveFilesWindowsOnly(steamApp);
        if (everythingInSaveFilesWindowsOnly.applies()) {
            return everythingInSaveFilesWindowsOnly;
        }
        EverythingInSaveFiles everythingInSaveFiles = new EverythingInSaveFiles(steamApp);
        if (everythingInSaveFiles.applies()) {
            return everythingInSaveFiles;
        }
        SaveFilesRootOverrides saveFilesRootOverrides = new SaveFilesRootOverrides(steamApp);
        if (saveFilesRootOverrides.applies()) {
            return saveFilesRootOverrides;
        }
        OnlyWindowsSaveFile onlyWindowsSaveFile = new OnlyWindowsSaveFile(steamApp);
        if (onlyWindowsSaveFile.applies()) {
            return onlyWindowsSaveFile;
        }
        throw new UnableToParseSaveException(steamApp.getName());
    }
}
