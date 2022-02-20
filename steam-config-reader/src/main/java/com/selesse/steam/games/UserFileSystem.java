package com.selesse.steam.games;

import com.selesse.steam.games.saves.SaveFile;
import com.selesse.steam.games.saves.SaveFilesFactory;
import com.selesse.steam.registry.implementation.RegistryStore;

public class UserFileSystem {
    private final RegistryStore gameRegistry;

    public UserFileSystem(RegistryStore gameRegistry) {
        this.gameRegistry = gameRegistry;
    }

    public String getMacInstallationPath() {
        return getSaveFile().getMacInfo().getSymbolPath();
    }

    public String getWindowsInstallationPath() {
        return getSaveFile().getWindowsInfo().getSymbolPath();
    }

    private SaveFile getSaveFile() {
        return SaveFilesFactory.determineSaveFile(gameRegistry);
    }
}
