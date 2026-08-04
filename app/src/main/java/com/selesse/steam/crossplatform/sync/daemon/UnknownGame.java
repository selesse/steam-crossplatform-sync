package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.crossplatform.sync.SteamCrossplatformSyncContext;
import com.selesse.steamcrossplatformsync.gamesessions.GameSession;
import com.selesse.steamcrossplatformsync.gamesessions.GameSessionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

record UnknownGame(long gameId, GameSession session) implements TrackedGame {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnknownGame.class);

    @Override
    public long getId() {
        return gameId;
    }

    @Override
    public void onClosed(SteamCrossplatformSyncContext context) {
        GameSessionRecord record = session.finish();
        context.getSessionRepository().save(record);
        LOGGER.info("Game closed (app ID: {}, name could not be resolved)", gameId);
        new SessionEndHook(record).runAsync(context.getConfig());
    }
}
