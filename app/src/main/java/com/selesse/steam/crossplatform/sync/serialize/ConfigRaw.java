package com.selesse.steam.crossplatform.sync.serialize;

import com.selesse.os.FilePathSanitizer;
import java.util.Optional;

public class ConfigRaw {
    public String pathToCloudStorage;
    public String gamesFileLocation;
    public String cloudStorageRelativeWritePath;
    public String remoteAppInfoUrl;

    public String getGamesFileLocation() {
        return Optional.ofNullable(gamesFileLocation)
                .map(FilePathSanitizer::sanitize)
                .orElse(null);
    }

    public String getPathToCloudStorage() {
        return Optional.ofNullable(pathToCloudStorage)
                .map(FilePathSanitizer::sanitize)
                .orElse(null);
    }

    public String getCloudStorageRelativeWritePath() {
        return Optional.ofNullable(cloudStorageRelativeWritePath)
                .map(FilePathSanitizer::sanitize)
                .orElse(null);
    }

    public String getRemoteAppInfoUrl() {
        return remoteAppInfoUrl;
    }
}
