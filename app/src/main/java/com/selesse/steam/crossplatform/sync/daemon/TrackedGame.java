package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.crossplatform.sync.SteamCrossplatformSyncContext;
import com.selesse.steamcrossplatformsync.gamesessions.GameSession;
import com.selesse.steamcrossplatformsync.gamesessions.GameSessionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

sealed interface TrackedGame permits KnownGame, UnknownGame {
    Logger LOGGER = LoggerFactory.getLogger(TrackedGame.class);

    long getId();

    GameSession session();

    void onClosed(SteamCrossplatformSyncContext context);

    /**
     * Closes out the session and records it, returning what happened.
     *
     * <p>Recording is best-effort. Syncing save files is what the app is for, and the session-end
     * hook may do things that matter on their own, so neither should be skipped just because
     * session tracking couldn't write to its database.
     */
    default GameSessionRecord finishSession(SteamCrossplatformSyncContext context) {
        GameSessionRecord record = session().finish();
        try {
            context.getSessionRepository().save(record);
        } catch (RuntimeException e) {
            LOGGER.warn("Could not record session for game {}", getId(), e);
        }
        return record;
    }
}
