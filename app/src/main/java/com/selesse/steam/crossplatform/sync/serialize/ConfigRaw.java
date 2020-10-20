package com.selesse.steam.crossplatform.sync.serialize;

public class ConfigRaw {
    public String localSyncLocation;
    public String gamesFileLocation;
    public String syncStorageRelativePath;

    public String getGamesFileLocation() {
        return gamesFileLocation;
    }

    public String getLocalSyncLocation() {
        return localSyncLocation;
    }

    public String getSyncStorageRelativePath() {
        return syncStorageRelativePath;
    }
}
