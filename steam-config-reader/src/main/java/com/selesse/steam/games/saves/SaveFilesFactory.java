package com.selesse.steam.games.saves;

import com.selesse.steam.SteamApp;

public class SaveFilesFactory {
    public static SaveFile determineSaveFile(SteamApp steamApp) {
        BypassRegistrySaveFile bypassRegistrySaveFile = new BypassRegistrySaveFile(steamApp);
        if (bypassRegistrySaveFile.applies()) {
            return bypassRegistrySaveFile;
        }
        return new EverythingInSaveFiles(steamApp);
    }
}
