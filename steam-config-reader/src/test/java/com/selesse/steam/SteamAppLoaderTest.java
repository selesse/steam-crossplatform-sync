package com.selesse.steam;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SteamAppLoaderTest {
    @Test
    public void canLoadHollowKnight() {
        TestGames hollowKnightGame = TestGames.HOLLOW_KNIGHT;

        SteamApp hollowKnightApp = SteamAppLoader.load(TestAppCache.PATH, hollowKnightGame.getGameId());

        assertThat(hollowKnightApp.getType()).isEqualTo(AppType.GAME);
    }
}
