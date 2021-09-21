package com.selesse.steam.registry.mac;

import com.selesse.steam.SteamApp;

import java.util.List;

public class SteamAppList {
    public List<SteamApp> apps;

    public SteamApp getAppById(Long gameId) {
        return apps.stream().filter(app -> app.appId == gameId).findFirst().orElseThrow();
    }
}
