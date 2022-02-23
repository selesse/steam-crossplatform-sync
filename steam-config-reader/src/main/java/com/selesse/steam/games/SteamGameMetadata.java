package com.selesse.steam.games;

public record SteamGameMetadata(long gameId, String name, boolean installed) {
    @Override
    public String toString() {
        return name + " (" + gameId + ") " + (installed ? "installed" : "not installed");
    }
}
