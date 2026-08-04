package com.selesse.steam.crossplatform.sync;

import java.util.List;
import java.util.Optional;

public record GameConfig(List<SyncableGame> games) {
    public Optional<SyncableGame> getGame(long gameId) {
        return games.stream().filter(game -> game.gameId() == gameId).findFirst();
    }
}
