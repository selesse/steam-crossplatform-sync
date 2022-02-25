package com.selesse.steam;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class SteamAppLoaderTest {
    @Before
    public void setup() {
        TestAppCache.setup();
    }

    @Test
    public void canLoadHollowKnight() {
        TestGames hollowKnightGame = TestGames.HOLLOW_KNIGHT;

        SteamApp hollowKnightApp = SteamAppLoader.load(hollowKnightGame.getGameId());

        assertThat(hollowKnightApp.getType()).isEqualTo(AppType.GAME);
    }
}
