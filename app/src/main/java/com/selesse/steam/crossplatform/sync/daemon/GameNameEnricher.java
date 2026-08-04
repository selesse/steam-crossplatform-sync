package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.crossplatform.sync.SteamCrossplatformSyncContext;
import com.selesse.steamcrossplatformsync.gamesessions.GameSessionRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Periodically resolves game names that could not be looked up when a session was first tracked.
 * Sessions with unresolvable names are stored as "Unknown Game (&lt;id&gt;)" and this enricher
 * attempts to backfill the real name when the AppCache becomes available.
 */
public class GameNameEnricher implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameNameEnricher.class);

    private final SteamCrossplatformSyncContext context;

    public GameNameEnricher(SteamCrossplatformSyncContext context) {
        this.context = context;
    }

    @Override
    public void run() {
        GameSessionRepository repository = context.getSessionRepository();
        List<Long> unknownIds = repository.findUnknownGameIds();
        if (unknownIds.isEmpty()) {
            return;
        }
        LOGGER.info("Attempting to resolve {} unknown game name(s)", unknownIds.size());
        for (long gameId : unknownIds) {
            try {
                var game = context.loadGame(gameId);
                repository.updateGameName(gameId, game.getName());
                LOGGER.info("Resolved game {}: {}", gameId, game.getName());
            } catch (RuntimeException e) {
                LOGGER.debug("Could not resolve name for game {}", gameId, e);
            }
        }
    }
}
