package com.selesse.steam.steamcmd.games;

import java.util.Objects;

public class SteamGameMetadata {
    private final long gameId;
    private final boolean installed;
    private final String name;

    public SteamGameMetadata(long gameId, String name, boolean installed) {
        this.gameId = gameId;
        this.name = name;
        this.installed = installed;
    }

    public long getGameId() {
        return gameId;
    }

    public boolean isInstalled() {
        return installed;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SteamGameMetadata that = (SteamGameMetadata) o;
        return gameId == that.gameId && installed == that.installed && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameId, installed, name);
    }

    @Override
    public String toString() {
        return name + " (" + gameId + ") " + (installed ? "installed" : "not installed");
    }
}
