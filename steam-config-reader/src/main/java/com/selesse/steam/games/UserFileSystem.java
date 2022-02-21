package com.selesse.steam.games;

import com.selesse.steam.SteamApp;
import com.selesse.steam.games.saves.SaveFile;
import com.selesse.steam.games.saves.SaveFilesFactory;

public class UserFileSystem {
    private final SteamApp steamApp;

    public UserFileSystem(SteamApp steamApp) {
        this.steamApp = steamApp;
    }

    public String getMacInstallationPath() {
        return getSaveFile().getMacInfo().getSymbolPath();
    }

    public String getWindowsInstallationPath() {
        return getSaveFile().getWindowsInfo().getSymbolPath();
    }

    private SaveFile getSaveFile() {
        return SaveFilesFactory.determineSaveFile(steamApp);
    }

    public String getLinuxInstallationPath() {
        return getSaveFile().getLinuxInfo().getSymbolPath();
    }
}
