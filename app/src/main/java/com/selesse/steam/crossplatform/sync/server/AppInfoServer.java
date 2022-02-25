package com.selesse.steam.crossplatform.sync.server;

import com.selesse.steam.crossplatform.sync.GameLoadingService;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steam.games.SteamGame;
import com.selesse.steam.registry.RegistryPrettyPrint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

public class AppInfoServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppInfoServer.class);
    private final SteamCrossplatformSyncConfig config;

    public AppInfoServer(SteamCrossplatformSyncConfig config) {
        this.config = config;
    }

    public void run() {
        Spark.get("/app-info/:app_id", (request, response) -> {
            String appId = request.params(":app_id");
            LOGGER.info("Received app print request for app ID {}", appId);
            SteamGame steamGame = new GameLoadingService(config).loadGame(Long.parseLong(appId));
            LOGGER.info("Printing registry file for {}", steamGame.metadata());
            return RegistryPrettyPrint.prettyPrint(steamGame.getRegistryStore());
        });
    }
}
