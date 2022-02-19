package com.selesse.steam;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SteamAppLoaderTest {
    @Test
    public void canLoadHollowKnight() {
        TestGames hollowKnightGame = TestGames.HOLLOW_KNIGHT;

        SteamApp hollowKnightApp = SteamAppLoader.load(hollowKnightGame.getGameId());

        assertThat(hollowKnightApp.getType()).isEqualTo(AppType.GAME);
    }
}