package com.selesse.steam.crossplatform.sync.daemon;

import java.util.Map;

class GameLoadErrorHook extends Hook {
    private final long gameId;
    private final RuntimeException error;

    GameLoadErrorHook(long gameId, RuntimeException error) {
        this.gameId = gameId;
        this.error = error;
    }

    @Override
    String name() {
        return "game-load-error";
    }

    @Override
    Map<String, String> env() {
        return Map.of(
                "STEAM_APP_ID",
                String.valueOf(gameId),
                "ERROR_MESSAGE",
                error.getMessage() != null ? error.getMessage() : "");
    }
}
