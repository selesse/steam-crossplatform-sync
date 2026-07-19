package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.crossplatform.sync.SteamCrossplatformSyncContext;
import com.selesse.steam.crossplatform.sync.SyncGameFilesService;
import com.selesse.steam.games.SteamGame;
import com.selesse.steamcrossplatformsync.gamesessions.GameSession;
import com.selesse.steamcrossplatformsync.gamesessions.GameSessionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

record KnownGame(SteamGame steamGame, GameSession session) implements TrackedGame {
    private static final Logger LOGGER = LoggerFactory.getLogger(KnownGame.class);

    @Override
    public long getId() {
        return steamGame.getId();
    }

    @Override
    public void onClosed(SteamCrossplatformSyncContext context) {
        GameSessionRecord record = session.finish();
        LOGGER.info("Game closed: {}", steamGame.getName());
        LOGGER.info("Running sync service for {}", steamGame.getName());
        // A sync failure shouldn't also silently skip the session-end hook - they're independent
        // concerns, and the hook may do things (e.g. notifications) that matter even when sync
        // itself couldn't run.
        try {
            new SyncGameFilesService(context).run(steamGame);
        } catch (RuntimeException e) {
            LOGGER.warn("Sync failed for {}", steamGame.getName(), e);
        }
        new SessionEndHook(record).runAsync(context.getConfig());
    }
}
