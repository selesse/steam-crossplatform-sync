package com.selesse.steam.crossplatform.sync;

import com.selesse.steam.crossplatform.sync.serialize.GameConfigRaw;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GameConfig {
    private final List<SyncableGame> games;

    private GameConfig(List<SyncableGame> games) {
        this.games = games;
    }

    public static GameConfig fromRaw(GameConfigRaw gameConfigRaw) {
        List<SyncableGame> games =
                gameConfigRaw.games.stream().map(SyncableGame::fromRaw).toList();
        return new GameConfig(games);
    }

    public List<SyncableGame> getGames() {
        return games;
    }

    public Optional<SyncableGame> getGame(Long gameId) {
        return games.stream()
                .filter(game -> Objects.equals(gameId, game.getGameId()))
                .findFirst();
    }
}
