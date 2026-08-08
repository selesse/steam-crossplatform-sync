package com.selesse.steam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.selesse.steam.registry.RegistryNotFoundException;
import java.util.List;
import org.junit.Test;

public class SteamAppLoaderTest {
    @Test
    public void canLoadHollowKnight() {
        TestGames hollowKnightGame = TestGames.HOLLOW_KNIGHT;

        SteamApp hollowKnightApp =
                new SteamAppLoader(TestSteamInstall.get()).load(TestAppCache.PATH, hollowKnightGame.getGameId());

        assertThat(hollowKnightApp.getType()).isEqualTo(AppType.GAME);
    }

    @Test
    public void canLoadSeveralGamesInOnePass() {
        List<Long> gameIds = List.of(TestGames.HOLLOW_KNIGHT.getGameId(), TestGames.OXYGEN_NOT_INCLUDED.getGameId());

        List<SteamApp> steamApps = new SteamAppLoader(TestSteamInstall.get()).loadSome(TestAppCache.PATH, gameIds);

        assertThat(steamApps).extracting(SteamApp::getType).containsExactly(AppType.GAME, AppType.GAME);
    }

    @Test
    public void loadSomeThrowsWhenAnIdIsMissing() {
        List<Long> gameIds = List.of(TestGames.HOLLOW_KNIGHT.getGameId(), 99L);

        SteamAppLoader steamAppLoader = new SteamAppLoader(TestSteamInstall.get());

        assertThatThrownBy(() -> steamAppLoader.loadSome(TestAppCache.PATH, gameIds))
                .isInstanceOf(RegistryNotFoundException.class);
    }
}
