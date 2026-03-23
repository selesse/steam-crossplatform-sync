package com.selesse.steam.crossplatform.sync;

import com.selesse.steam.SteamAppLoader;
import com.selesse.steam.games.SteamGame;
import java.util.List;

public class GameLoadingService {
    public GameLoadingService() {}

    public SteamGame loadGame(long gameId) {
        return new SteamGame(SteamAppLoader.load(gameId).getRegistryStore());
    }

    public List<SteamGame> loadGames(List<Long> gameIds) {
        return gameIds.stream().map(this::loadGame).toList();
    }
}
