package com.selesse.steam.games;

public record SteamGameMetadata(long gameId, String name) {
    @Override
    public String toString() {
        return name + " (" + gameId + ")";
    }
}
