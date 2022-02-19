package com.selesse.steam.applist;

import java.util.List;

public class SteamAppList {
    public List<SteamApp> apps;

    public SteamApp getAppById(Long gameId) {
        return apps.stream().filter(app -> app.appId == gameId).findFirst().orElseThrow();
    }
}
