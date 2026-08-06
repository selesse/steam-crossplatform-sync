package com.selesse.steam.crossplatform.sync;

import com.google.common.collect.Lists;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.SteamApp;
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
        List<SteamApp> steamApps = context.loadInstalledGames();

        List<SyncableGame> syncableGames = Lists.newArrayList();

        for (SteamApp steamApp : steamApps) {
            if (steamApp.hasUserFileSystem() && steamApp.hasAnySavePaths()) {
                List<String> windowsPaths = getPathsOrNull(steamApp, OperatingSystems.OperatingSystem.WINDOWS);
                List<String> macPaths = getPathsOrNull(steamApp, OperatingSystems.OperatingSystem.MAC);
                List<String> linuxPaths = getPathsOrNull(steamApp, OperatingSystems.OperatingSystem.LINUX);

                SyncableGame syncableGame = new SyncableGame(
                        steamApp.getName(), windowsPaths, macPaths, linuxPaths, steamApp.getId(), true);
                syncableGames.add(syncableGame);
            }
        }

        syncableGames.sort(Comparator.comparing(SyncableGame::name));
        YAMLFactory yamlFactory = new YAMLFactory();
        ObjectMapper objectMapper = new ObjectMapper(yamlFactory);
        objectMapper.writeValue(System.out, new GameConfig(syncableGames));
    }

    // Null rather than an empty list, so games.yml simply omits the key for an OS the game
    // doesn't run on. Windows is always emitted: a game with no oslist is treated as
    // Windows-only, and Windows-rooted save paths are the fallback shape regardless.
    private List<String> getPathsOrNull(SteamApp steamApp, OperatingSystems.OperatingSystem os) {
        if (os != OperatingSystems.OperatingSystem.WINDOWS && !steamApp.runsOn(os)) {
            return null;
        }
        return steamApp.getSavePaths(os).stream()
                .map(UserFileSystemPath::getSymbolPath)
                .toList();
    }
}
