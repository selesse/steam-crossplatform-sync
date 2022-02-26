package com.selesse.steam.games;

import java.util.List;

public class SteamGameSave {
    private List<UserFileSystemPath> userFileSystemPaths;

    public SteamGameSave(List<UserFileSystemPath> userFileSystemPaths) {
        this.userFileSystemPaths = userFileSystemPaths;
    }
}
