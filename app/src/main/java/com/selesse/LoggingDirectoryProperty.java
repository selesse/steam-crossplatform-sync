package com.selesse;

import ch.qos.logback.core.PropertyDefinerBase;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSync;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;

public class LoggingDirectoryProperty extends PropertyDefinerBase {
    @Override
    public String getPropertyValue() {
        SteamCrossplatformSyncConfig config = SteamCrossplatformSync.loadConfiguration();
        return config.getConfigDirectory().toAbsolutePath().toString();
    }
}
