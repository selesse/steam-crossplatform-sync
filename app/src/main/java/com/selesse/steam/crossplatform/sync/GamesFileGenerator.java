package com.selesse.steam.crossplatform.sync;

import com.google.common.collect.Lists;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.crossplatform.sync.serialize.GameConfigRaw;
import com.selesse.steam.crossplatform.sync.serialize.SyncableGameRaw;
import com.selesse.steam.games.SteamGame;
import com.selesse.steam.games.UserFileSystemPath;
import java.util.Comparator;
import java.util.List;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

public class GamesFileGenerator {
    private final SteamCrossplatformSyncContext context;

    public GamesFileGenerator(SteamCrossplatformSyncContext config) {
        this.context = config;
    }

    public void run() {
        List<SteamGame> steamGames = context.fetchAllGamesOrLoadInstalledGames();

        List<SyncableGameRaw> syncableGames = Lists.newArrayList();

        for (SteamGame steamGame : steamGames) {
            if (steamGame.hasUserCloud() && steamGame.hasComputedInstallationPath()) {
                List<String> windowsPaths = getPathsOrNull(steamGame, OperatingSystems.OperatingSystem.WINDOWS);
                List<String> macPaths = getPathsOrNull(steamGame, OperatingSystems.OperatingSystem.MAC);
                List<String> linuxPaths = getPathsOrNull(steamGame, OperatingSystems.OperatingSystem.LINUX);

                SyncableGameRaw syncableGame = new SyncableGameRaw(
                        windowsPaths, macPaths, linuxPaths, steamGame.getName(), steamGame.getId(), true);
                syncableGames.add(syncableGame);
            }
        }

        syncableGames.sort(Comparator.comparing(SyncableGameRaw::name));
        GameConfigRaw gameConfigRaw = new GameConfigRaw();
        gameConfigRaw.games = syncableGames;
        YAMLFactory yamlFactory = new YAMLFactory();
        ObjectMapper objectMapper = new ObjectMapper(yamlFactory);
        objectMapper.writeValue(System.out, gameConfigRaw);
    }

    private List<String> getPathsOrNull(SteamGame steamGame, OperatingSystems.OperatingSystem os) {
        var installationPaths =
                switch (os) {
                    case MAC -> steamGame.getMacInstallationPaths();
                    case WINDOWS -> steamGame.getWindowsInstallationPaths();
                    case LINUX, STEAM_OS -> steamGame.getLinuxInstallationPaths();
                };
        if (installationPaths == null) {
            return null;
        }
        return installationPaths.stream().map(UserFileSystemPath::getSymbolPath).toList();
    }
}
