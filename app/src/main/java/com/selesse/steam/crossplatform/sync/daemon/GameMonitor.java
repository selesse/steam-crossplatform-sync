package com.selesse.steam.crossplatform.sync.daemon;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.GameRunningDetector;
import com.selesse.steam.crossplatform.sync.SteamCrossplatformSyncContext;
import com.selesse.steam.crossplatform.sync.SyncGameFilesService;
import com.selesse.steam.games.SteamGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class GameMonitor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMonitor.class);

    private final SteamCrossplatformSyncContext context;
    private SteamGame runningGame;

    public GameMonitor(SteamCrossplatformSyncContext context) {
        this.context = context;
    }

    @Override
    public void run() {
        synchronized (this) {
            if (GameRunningDetector.isGameCurrentlyRunning()) {
                long currentGameId = GameRunningDetector.getCurrentlyRunningGameId();

                if (runningGame == null) {
                    runningGame = context.loadGame(currentGameId);
                    onGameLaunch(runningGame);
                } else if (currentGameId != runningGame.getId()) {
                    SteamGame newGame = context.loadGame(currentGameId);
                    LOGGER.info("Game switch detected, closed {} but opened {}", runningGame.getName(), newGame.getName());
                    LOGGER.info("Game closed: {}", runningGame.getName());
                    runningGame = newGame;
                    onGameLaunch(newGame);
                }
            } else if (runningGame != null) {
                LOGGER.info("Game closed: {}", runningGame.getName());
                LOGGER.info("Running sync service for {}", runningGame.getName());
                new SyncGameFilesService(context.getConfig()).run(runningGame.getId());
                runningGame = null;
            }
        }
    }

    private void onGameLaunch(SteamGame runningGame) {
        LOGGER.info("Game launched: {}", runningGame.getName());

        findGameOverlayProcess().ifPresentOrElse(processHandle -> {
            processHandle.onExit().thenRunAsync(this);
        }, () -> LOGGER.info("Couldn't find GameOverlay process"));
    }

    private Optional<ProcessHandle> findGameOverlayProcess() {
        return ProcessHandle.allProcesses().filter(ProcessHandle::isAlive).filter(p -> {
            String gameOverlayProcess = getGameOverlayProcessName();
            List<String> processArguments =
                    p.info().command().map(x -> Splitter.on(File.separatorChar).splitToList(x)).orElse(Lists.newArrayList());
            return processArguments.size() > 0 && processArguments.get(processArguments.size() - 1).equalsIgnoreCase(gameOverlayProcess);
        }).findFirst();
    }

    private String getGameOverlayProcessName() {
        return OperatingSystems.get() == OperatingSystems.OperatingSystem.WINDOWS ? "GameOverlayUI.exe" : "gameoverlayui";
    }
}
