package com.selesse.steam.games.saves;

import com.selesse.os.OperatingSystems.OperatingSystem;
import com.selesse.steam.SteamApp;
import com.selesse.steam.games.UserFileSystemPath;
import com.selesse.steam.registry.implementation.RegistryObject;
import java.util.List;

public abstract class SaveFile {
    protected final SteamApp steamApp;
    protected final RegistryObject ufs;

    public SaveFile(SteamApp steamApp) {
        this.steamApp = steamApp;
        this.ufs = steamApp.getRegistryObject().getObjectValueAsObject("ufs");
    }

    public abstract boolean applies();

    /**
     * Where this app's saves live when running on {@code os}. Only windows/macos/linux are ever
     * passed in - SteamOS is normalized to Linux upstream, in {@link SteamApp#getSavePaths}.
     */
    public abstract List<UserFileSystemPath> savePathsFor(OperatingSystem os);
}
