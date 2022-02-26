package com.selesse.steam.crossplatform.sync;

import com.selesse.steam.SteamAccountId;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSync;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steam.games.RemoteGameListFetcher;
import com.selesse.steam.games.SteamGame;
import com.selesse.steam.user.SteamAccountIdFinder;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SteamCrossplatformSyncContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(SteamCrossplatformSyncContext.class);

    private final SteamCrossplatformSyncConfig config;
    private final GameLoadingService gameLoadingService;

    public SteamCrossplatformSyncContext() {
        this.config = SteamCrossplatformSync.loadConfiguration();
        this.gameLoadingService = new GameLoadingService(config);
    }

    public SteamCrossplatformSyncConfig getConfig() {
        return config;
    }

    public SteamGame loadGame(long gameId) {
        return gameLoadingService.loadGame(gameId);
    }

    // The locally installed game list is pretty isn't representative of the games that you have on your account because
    // it's OS-specific. It's better if the functionality that relies on listing your games behaves more holistically,
    // so that generating your game list is the same no matter what OS you try to run it on.
    public List<SteamGame> fetchAllGamesOrLoadInstalledGames() {
        Optional<SteamAccountId> steamAccountIdMaybe = SteamAccountIdFinder.findIfPresent();
        var steamGamesMaybe = steamAccountIdMaybe.map(this::attemptToFetchPublicGamesList);
        return steamGamesMaybe.orElse(loadInstalledGames());
    }

    private List<SteamGame> loadInstalledGames() {
        return gameLoadingService.loadInstalledGames();
    }

    private List<SteamGame> attemptToFetchPublicGamesList(SteamAccountId accountId) {
        LOGGER.info("Fetching games.xml with steam ID {} to get games list", accountId.to64Bit());
        return new RemoteGameListFetcher(accountId)
                .fetchGameIdList().stream().map(this::loadGame).toList();
    }
}
