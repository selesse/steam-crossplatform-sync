package com.selesse.steam.crossplatform.sync.server;

import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppInfoServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppInfoServer.class);
    private final SteamCrossplatformSyncConfig config;

    public AppInfoServer(SteamCrossplatformSyncConfig config) {
        this.config = config;
    }

    public void run() {}
}
