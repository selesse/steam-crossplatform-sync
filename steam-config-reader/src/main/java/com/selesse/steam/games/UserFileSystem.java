package com.selesse.steam.games;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.SteamApp;
import com.selesse.steam.games.saves.SaveFile;
import com.selesse.steam.games.saves.SaveFilesFactory;
import java.util.List;

public class UserFileSystem {
    private final SteamApp steamApp;

    public UserFileSystem(SteamApp steamApp) {
        this.steamApp = steamApp;
    }

    public String getWindowsInstallationPath() {
        return getSaveFile().getWindowsInfo().getSymbolPath();
    }

    public List<UserFileSystemPath> getWindowsInstallationPaths() {
        return getSaveFile().getWindowsSavePaths();
    }

    public String getMacInstallationPath() {
        if (steamApp.getSupportedOperatingSystems().contains(OperatingSystems.OperatingSystem.MAC)) {
            return getSaveFile().getMacInfo().getSymbolPath();
        }
        return null;
    }

    public List<UserFileSystemPath> getMacInstallationPaths() {
        if (steamApp.getSupportedOperatingSystems().contains(OperatingSystems.OperatingSystem.MAC)) {
            return getSaveFile().getMacSavePaths();
        }
        return null;
    }

    public String getLinuxInstallationPath() {
        if (steamApp.getSupportedOperatingSystems().contains(OperatingSystems.OperatingSystem.LINUX)) {
            return getSaveFile().getLinuxInfo().getSymbolPath();
        }
        return null;
    }

    public List<UserFileSystemPath> getLinuxInstallationPaths() {
        if (steamApp.getSupportedOperatingSystems().contains(OperatingSystems.OperatingSystem.LINUX)) {
            return getSaveFile().getLinuxSavePaths();
        }
        return null;
    }

    private SaveFile getSaveFile() {
        return SaveFilesFactory.determineSaveFile(steamApp);
    }
}
