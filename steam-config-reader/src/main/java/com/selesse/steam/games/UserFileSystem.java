package com.selesse.steam.games;

import com.selesse.os.OperatingSystems.OperatingSystem;
import com.selesse.steam.SteamApp;
import com.selesse.steam.games.saves.SaveFilesFactory;
import java.util.List;

public class UserFileSystem {
    private final SteamApp steamApp;

    public UserFileSystem(SteamApp steamApp) {
        this.steamApp = steamApp;
    }

    /**
     * Where this app's saves live when running on {@code os}, or empty if it has none there.
     */
    public List<UserFileSystemPath> getSavePaths(OperatingSystem os) {
        // ufs entries and rootoverrides only ever describe windows/macos/linux, so SteamOS reads
        // as Linux from here down. Windows is never gated on declared support: an app with no
        // oslist is treated as Windows-only, and Windows-rooted ufs entries are the fallback
        // shape even for apps that don't list Windows.
        OperatingSystem target = os == OperatingSystem.STEAM_OS ? OperatingSystem.LINUX : os;
        if (target != OperatingSystem.WINDOWS && !steamApp.supports(target)) {
            return List.of();
        }
        return SaveFilesFactory.determineSaveFile(steamApp).savePathsFor(target);
    }
}
