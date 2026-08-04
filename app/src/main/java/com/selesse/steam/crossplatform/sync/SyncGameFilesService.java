package com.selesse.steam.crossplatform.sync;

import com.google.common.annotations.VisibleForTesting;
import com.selesse.files.PatternSupportedPath;
import com.selesse.files.SyncablePath;
import com.selesse.steam.SteamApp;
import com.selesse.steam.crossplatform.sync.config.GamesToSyncLoader;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncGameFilesService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncGameFilesService.class);
    private final SteamCrossplatformSyncContext context;

    public SyncGameFilesService(SteamCrossplatformSyncContext context) {
        this.context = context;
    }

    public void runForAllGames() {
        GameConfig gameList = new GamesToSyncLoader().loadGames(context.getConfig());
        gameList.games().forEach(this::sync);
    }

    public void run(SteamApp steamApp) {
        run(new SteamApp[] {steamApp});
    }

    public void run(Long[] gameIds) {
        Arrays.stream(gameIds).map(context::loadGame).toList().forEach(this::run);
    }

    public void run(SteamApp[] steamApps) {
        List<SteamApp> gamesToSync = Arrays.stream(steamApps).toList();
        GameConfig gameList = new GamesToSyncLoader().loadGames(context.getConfig());
        for (SteamApp steamApp : gamesToSync) {
            gameList.getGame(steamApp.getId())
                    .ifPresentOrElse(
                            this::sync, () -> LOGGER.warn("Could not find game config for {}", steamApp.getName()));
        }
    }

    @VisibleForTesting
    void sync(SyncableGame game) {
        if (game.isSupportedOnThisOs()) {
            if (!game.sync()) {
                LOGGER.info("Not syncing {} due to its configuration", game.name());
                return;
            }
            LOGGER.info("Checking {}", game.name());

            List<PatternSupportedPath> localPaths = game.getLocalPaths();
            for (PatternSupportedPath localPath : localPaths) {
                var steamAccountIdMaybe = context.getSteamAccountIdIfPresent();
                PatternSupportedPath parent = localPath.getParent();
                if (steamAccountIdMaybe != null) {
                    if (parent.endsWith(steamAccountIdMaybe.to64Bit())) {
                        parent = parent.getParent();
                    }
                }
                SyncablePath syncableLocalPath = new SyncablePath(parent, localPath);
                PatternSupportedPath cloudPath =
                        PatternSupportedPath.fromPath(game.getLocalCloudSyncPath(context.getConfig()));
                PatternSupportedPath relativeToCloudPath = cloudPath.resolve(parent.relativize(localPath));
                SyncablePath syncableLocalCloudPath = new SyncablePath(cloudPath, relativeToCloudPath);
                GameSyncer.sync(syncableLocalPath, syncableLocalCloudPath);
            }
        } else {
            LOGGER.info("Did not check {} because it is unsupported on this OS", game.name());
        }
    }
}
