package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.crossplatform.sync.SteamCrossplatformSyncContext;
import com.selesse.steamcrossplatformsync.gamesessions.GameSession;

sealed interface TrackedGame permits KnownGame, UnknownGame {
    long getId();

    GameSession session();

    void onClosed(SteamCrossplatformSyncContext context);
}
