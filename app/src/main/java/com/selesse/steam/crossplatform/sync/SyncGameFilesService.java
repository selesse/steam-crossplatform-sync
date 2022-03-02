package com.selesse.steam.crossplatform.sync;

import com.selesse.files.SyncablePath;
import com.selesse.steam.crossplatform.sync.config.GamesToSyncLoader;
import com.selesse.steam.games.SteamGame;
import java.nio.file.Path;
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
        gameList.getGames().forEach(this::sync);
    }

    public void run(SteamGame steamGame) {
        run(new SteamGame[] {steamGame});
    }

    public void run(Long[] gameIds) {
        Arrays.stream(gameIds).map(context::loadGame).toList().forEach(this::run);
    }

    public void run(SteamGame[] steamGames) {
        List<SteamGame> gamesToSync = Arrays.stream(steamGames).toList();
        GameConfig gameList = new GamesToSyncLoader().loadGames(context.getConfig());
        for (SteamGame steamGame : gamesToSync) {
            gameList.getGame(steamGame.getId())
                    .ifPresentOrElse(
                            this::sync, () -> LOGGER.warn("Could not find game config for {}", steamGame.getName()));
        }
    }

    private void sync(SyncableGame game) {
        if (game.isSupportedOnThisOs()) {
            if (!game.sync()) {
                LOGGER.info("Not syncing {} due to its configuration", game.getName());
                return;
            }
            LOGGER.info("Checking {}", game.getName());

            SyncablePath syncableLocalCloudPath = new SyncablePath(game.getLocalCloudSyncPath(context.getConfig()));
            List<Path> localPaths = game.getLocalPaths();
            for (Path localPath : localPaths) {
                SyncablePath syncableLocalPath = new SyncablePath(localPath.getParent(), localPath);
                GameSyncer.sync(syncableLocalPath, syncableLocalCloudPath);
            }
        } else {
            LOGGER.info("Did not check {} because it is unsupported on this OS", game.getName());
        }
    }
}
