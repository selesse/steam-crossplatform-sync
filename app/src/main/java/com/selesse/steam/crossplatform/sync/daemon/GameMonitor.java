package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.GameRunningDetector;
import com.selesse.steam.Games;
import com.selesse.steam.crossplatform.sync.SyncGameFilesService;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steam.steamcmd.SteamGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class GameMonitor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMonitor.class);

    private final SteamCrossplatformSyncConfig config;
    private SteamGame runningGame;

    public GameMonitor(SteamCrossplatformSyncConfig config) {
        this.config = config;
        if (GameRunningDetector.isGameCurrentlyRunning()) {
            this.runningGame = Games.loadGame(
                    config.getConfigDirectory(),
                    config.getRemoteAppInfoUrl(),
                    GameRunningDetector.getCurrentlyRunningGameId()
            );
        }
    }

    @Override
    public void run() {
        if (GameRunningDetector.isGameCurrentlyRunning()) {
            long currentGameId = GameRunningDetector.getCurrentlyRunningGameId();

            if (runningGame == null) {
                Optional<SteamGame> steamGameMaybe = loadGame(currentGameId);
                steamGameMaybe.ifPresentOrElse(steamGame -> {
                    runningGame = steamGame;
                    LOGGER.info("Game launched: {}", runningGame.getName());
                }, () -> LOGGER.info("Game launched with ID {}", currentGameId));
            }
            else if (currentGameId != runningGame.getId()) {
                Optional<SteamGame> currentSteamGame = loadGame(currentGameId);
                SteamGame newGame = currentSteamGame.orElse(null);
                String newGameName = currentSteamGame.map(SteamGame::getName).orElse("" + currentGameId);
                LOGGER.info("Game switch detected, closed {} but opened {}", runningGame.getName(), newGameName);
                runningGame = newGame;
            }
        } else if (runningGame != null) {
            LOGGER.info("Game closed: {}", runningGame.getName());
            LOGGER.info("Running sync service for {}", runningGame.getName());
            new SyncGameFilesService(config).run(runningGame.getId());
            runningGame = null;
        }
    }

    private Optional<SteamGame> loadGame(long gameId) {
        try {
            return Optional.of(Games.loadGame(config.getCacheDirectory(), config.getRemoteAppInfoUrl(), gameId));
        } catch (RuntimeException e) {
            LOGGER.error("Error trying to load gameId {}", gameId, e);
            return Optional.empty();
        }
    }
}
