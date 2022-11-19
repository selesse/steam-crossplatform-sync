package com.selesse.steamcrossplatformsync.gamesessions;

import com.selesse.steam.games.SteamGame;
import java.time.OffsetDateTime;

record GameSessionRecord(OffsetDateTime startedAt, OffsetDateTime finishedAt, SteamGame game, String hostname) {}
