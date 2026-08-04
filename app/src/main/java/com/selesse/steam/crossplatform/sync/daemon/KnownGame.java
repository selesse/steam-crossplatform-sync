package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.SteamApp;
import com.selesse.steam.crossplatform.sync.SteamCrossplatformSyncContext;
import com.selesse.steam.crossplatform.sync.SyncGameFilesService;
import com.selesse.steamcrossplatformsync.gamesessions.GameSession;
import com.selesse.steamcrossplatformsync.gamesessions.GameSessionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

record KnownGame(SteamApp steamApp, GameSession session) implements TrackedGame {
    private static final Logger LOGGER = LoggerFactory.getLogger(KnownGame.class);

    @Override
    public long getId() {
        return steamApp.getId();
    }

    @Override
    public void onClosed(SteamCrossplatformSyncContext context) {
        GameSessionRecord record = session.finish();
        context.getSessionRepository().save(record);
        LOGGER.info("Game closed: {}", steamApp.getName());
        LOGGER.info("Running sync service for {}", steamApp.getName());
        // A sync failure shouldn't also silently skip the session-end hook - they're independent
        // concerns, and the hook may do things (e.g. notifications) that matter even when sync
        // itself couldn't run.
        try {
            new SyncGameFilesService(context).run(steamApp);
        } catch (RuntimeException e) {
            LOGGER.warn("Sync failed for {}", steamApp.getName(), e);
        }
        new SessionEndHook(record).runAsync(context.getConfig());
    }
}
