package com.selesse.steam.crossplatform.sync.serialize;

import com.selesse.os.FilePathSanitizer;

public class ConfigRaw {
    public String pathToCloudStorage;
    public String gamesFileLocation;
    public String cloudStorageRelativeWritePath;
    public String cloudProvider;

    public String getGamesFileLocation() {
        return sanitized(gamesFileLocation);
    }

    public String getPathToCloudStorage() {
        return sanitized(pathToCloudStorage);
    }

    public String getCloudStorageRelativeWritePath() {
        return sanitized(cloudStorageRelativeWritePath);
    }

    private static String sanitized(String raw) {
        return raw == null ? null : FilePathSanitizer.sanitize(raw);
    }
}
