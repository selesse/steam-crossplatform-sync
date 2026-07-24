package com.selesse.steam.games;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.selesse.steam.AppCacheReader;
import com.selesse.steam.appcache.App;
import com.selesse.steam.appcache.VdfObject;
import com.selesse.steam.appcache.VdfString;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class InstalledGameFinderServiceTest {
    @Test
    public void findKeepsOnlyGameTypeAppsAndExcludesEntriesMissingFromTheCache() {
        long gameId = 1L;
        long dlcId = 2L;
        long notInCacheId = 3L;
        InstalledGameFetcher fetcher = () -> List.of(gameId, dlcId, notInCacheId);
        AppCacheReader appCacheReader = mock(AppCacheReader.class);
        doReturn(Map.of(gameId, appOfType("game"), dlcId, appOfType("dlc")))
                .when(appCacheReader)
                .loadSome(Set.of(gameId, dlcId, notInCacheId));

        InstalledGameFinderService service = new InstalledGameFinderService(List.of(fetcher), appCacheReader);

        assertThat(service.find()).containsExactly(gameId);
    }

    @Test
    public void findThrowsWhenNoneOfTheInstalledAppsAreGames() {
        InstalledGameFetcher fetcher = () -> List.of(1L);
        AppCacheReader appCacheReader = mock(AppCacheReader.class);
        doReturn(Map.of(1L, appOfType("tool"))).when(appCacheReader).loadSome(Set.of(1L));

        InstalledGameFinderService service = new InstalledGameFinderService(List.of(fetcher), appCacheReader);

        assertThatThrownBy(service::find).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void findFallsBackToTheNextFetcherWhenTheFirstFindsNoGames() {
        InstalledGameFetcher emptyFetcher = List::of;
        InstalledGameFetcher fallbackFetcher = () -> List.of(1L);
        AppCacheReader appCacheReader = mock(AppCacheReader.class);
        doReturn(Map.of(1L, appOfType("game"))).when(appCacheReader).loadSome(Set.of(1L));

        InstalledGameFinderService service =
                new InstalledGameFinderService(List.of(emptyFetcher, fallbackFetcher), appCacheReader);

        assertThat(service.find()).containsExactly(1L);
    }

    private App appOfType(String type) {
        VdfObject common = new VdfObject("common");
        common.add(new VdfString("type", type));
        VdfObject root = new VdfObject("appinfo");
        root.add(common);
        return new App(0, 0, 0, 0, 0, new byte[20], 0, null, root);
    }
}
