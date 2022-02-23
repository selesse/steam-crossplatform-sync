package com.selesse.steam;

import com.selesse.steam.applist.SteamAppList;
import com.selesse.steam.applist.SteamNameAndId;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AppListFetcherTest {
    @Ignore(value = "This sends a live HTTP request, only run it manually...")
    @Test
    public void testCanFetchAppList() {
        SteamAppList steamAppList = AppListFetcher.fetchAppList();
        SteamNameAndId hollowKnight = steamAppList.getAppById(TestGames.HOLLOW_KNIGHT.getGameId());
        assertThat(hollowKnight.name()).isEqualTo("Hollow Knight");
    }
}