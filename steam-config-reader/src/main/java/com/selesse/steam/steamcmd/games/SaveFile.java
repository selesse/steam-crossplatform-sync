package com.selesse.steam.steamcmd.games;

public interface SaveFile {
    boolean applies();
    UserFileSystemPath getMacInfo();
    UserFileSystemPath getWindowsInfo();
}
