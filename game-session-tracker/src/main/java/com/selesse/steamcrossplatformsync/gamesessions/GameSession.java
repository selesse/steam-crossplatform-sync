package com.selesse.steamcrossplatformsync.gamesessions;

import com.selesse.os.Hostnames;
import com.selesse.steam.games.SteamGame;
import java.time.OffsetDateTime;

public class GameSession {
    private final OffsetDateTime startedAt;
    private final SteamGame game;

    private GameSession(SteamGame steamGame) {
        this.game = steamGame;
        this.startedAt = OffsetDateTime.now();
    }

    public static GameSession start(SteamGame steamGame) {
        return new GameSession(steamGame);
    }

    public void finish() {
        var finishedAt = OffsetDateTime.now();

        GameSessionRepository.getInstance()
                .save(new GameSessionRecord(startedAt, finishedAt, game, Hostnames.getCurrent()));
    }
}
