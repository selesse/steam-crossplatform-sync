package com.selesse.steam.crossplatform.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import com.selesse.steam.Games;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steam.crossplatform.sync.serialize.GameConfigRaw;
import com.selesse.steam.crossplatform.sync.serialize.SyncableGameRaw;
import com.selesse.steam.steamcmd.SteamGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public class GamesFileGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(GamesFileGenerator.class);
    private final SteamCrossplatformSyncConfig config;

    public GamesFileGenerator(SteamCrossplatformSyncConfig config) {
        this.config = config;
    }

    public void run() {
        Path gamesConfigPath = config.getGamesFile();
        List<SteamGame> steamGames = Games.loadInstalledGames(config.getConfigDirectory());

        List<SyncableGameRaw> syncableGames = Lists.newArrayList();

        for (SteamGame steamGame : steamGames) {
            if (steamGame.hasUserCloud()) {
                try {
                    String windowsPath = steamGame.getWindowsInstallationPath();
                    String macPath = steamGame.getMacInstallationPath();

                    SyncableGameRaw syncableGame = new SyncableGameRaw();
                    syncableGame.mac = macPath;
                    syncableGame.windows = windowsPath;
                    syncableGame.name = steamGame.getName();
                    syncableGame.gameId = steamGame.getId();
                    syncableGames.add(syncableGame);
                } catch (RuntimeException e) {
                    LOGGER.error("Unable to get install locations for {}", steamGame.getName(), e);
                }
            }
        }

        syncableGames.sort(Comparator.comparing(x -> x.name));
        GameConfigRaw gameConfigRaw = new GameConfigRaw();
        gameConfigRaw.games = syncableGames;
        YAMLFactory yamlFactory = new YAMLFactory();
        ObjectMapper objectMapper = new ObjectMapper(yamlFactory);
        try {
            objectMapper.writeValue(gamesConfigPath.toFile(), gameConfigRaw);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
