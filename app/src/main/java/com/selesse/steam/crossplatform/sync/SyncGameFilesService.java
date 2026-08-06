package com.selesse.steam.crossplatform.sync;

import com.google.common.annotations.VisibleForTesting;
import com.selesse.files.PatternSupportedPath;
import com.selesse.files.SyncablePath;
import com.selesse.steam.SteamApp;
import com.selesse.steam.crossplatform.sync.config.GamesToSyncLoader;
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
        run(steamApp.getId());
    }

    /**
     * Syncs each game's configured save paths. Which paths those are is keyed by app ID alone, so
     * nothing here needs the app cache entry - naming a game is left to the one branch that has
     * something to say about it.
     */
    public void run(Long... gameIds) {
        GameConfig gameList = new GamesToSyncLoader().loadGames(context.getConfig());
        for (Long gameId : gameIds) {
            gameList.getGame(gameId)
                    .ifPresentOrElse(this::sync, () -> LOGGER.warn("Could not find game config for {}", name(gameId)));
        }
    }

    /**
     * Only for the warning above: a game missing from games.yml is something the user has to go add,
     * and an app ID on its own isn't much to go on. Worth an app cache read on that path, not on
     * the one where the sync just works.
     */
    private String name(long gameId) {
        try {
            return context.loadGame(gameId).getName() + " (" + gameId + ")";
        } catch (RuntimeException e) {
            return "app ID " + gameId;
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
