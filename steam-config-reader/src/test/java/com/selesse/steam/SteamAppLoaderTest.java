package com.selesse.steam;

import com.selesse.os.Resources;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SteamAppLoaderTest {
    @Before
    public void setup() {
        SteamAppLoader.primeAppCache(new AppCacheReader().load(Resources.getResource("appinfo.vdf")));
    }

    @Test
    public void canLoadHollowKnight() {
        TestGames hollowKnightGame = TestGames.HOLLOW_KNIGHT;

        SteamApp hollowKnightApp = SteamAppLoader.load(hollowKnightGame.getGameId());

        assertThat(hollowKnightApp.getType()).isEqualTo(AppType.GAME);
    }
}