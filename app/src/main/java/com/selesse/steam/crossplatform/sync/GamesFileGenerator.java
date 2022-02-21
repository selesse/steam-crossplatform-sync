package com.selesse.steam.crossplatform.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import com.selesse.steam.crossplatform.sync.serialize.GameConfigRaw;
import com.selesse.steam.crossplatform.sync.serialize.SyncableGameRaw;
import com.selesse.steam.games.SteamGame;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class GamesFileGenerator {
    private final SteamCrossplatformSyncContext context;

    public GamesFileGenerator(SteamCrossplatformSyncContext config) {
        this.context = config;
    }

    public void run() {
        List<SteamGame> steamGames = context.loadInstalledGames();

        List<SyncableGameRaw> syncableGames = Lists.newArrayList();

        for (SteamGame steamGame : steamGames) {
            if (steamGame.hasUserCloud() && steamGame.hasComputedInstallationPath()) {
                String windowsPath = steamGame.getWindowsInstallationPath();
                String macPath = steamGame.getMacInstallationPath();

                SyncableGameRaw syncableGame = new SyncableGameRaw();
                syncableGame.mac = macPath;
                syncableGame.windows = windowsPath;
                syncableGame.name = steamGame.getName();
                syncableGame.gameId = steamGame.getId();
                syncableGames.add(syncableGame);
            }
        }

        syncableGames.sort(Comparator.comparing(x -> x.name));
        GameConfigRaw gameConfigRaw = new GameConfigRaw();
        gameConfigRaw.games = syncableGames;
        YAMLFactory yamlFactory = new YAMLFactory();
        ObjectMapper objectMapper = new ObjectMapper(yamlFactory);
        try {
            objectMapper.writeValue(System.out, gameConfigRaw);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
