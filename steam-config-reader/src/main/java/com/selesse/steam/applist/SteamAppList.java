package com.selesse.steam.applist;

import java.util.List;

public record SteamAppList(List<SteamNameAndId> apps) {
    public SteamNameAndId getAppById(Long gameId) {
        return apps.stream().filter(app -> app.appId() == gameId).findFirst().orElseThrow();
    }
}
