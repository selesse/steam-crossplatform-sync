package com.selesse.steam.games.saves;

import com.selesse.steam.SteamApp;

public class SaveFilesFactory {
    public static SaveFile determineSaveFile(SteamApp steamApp) {
        return new EverythingInSaveFiles(steamApp);
    }
}
