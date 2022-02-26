package com.selesse.steam.crossplatform.sync;

import com.selesse.caches.FileBackedCache;
import com.selesse.caches.FileBackedCacheBuilder;
import com.selesse.files.FileModified;
import com.selesse.steam.GameRegistries;
import com.selesse.steam.SteamAccountId;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import com.selesse.steam.games.RemoteGameListFetcher;
import com.selesse.steam.games.SteamGame;
import com.selesse.steam.games.SteamGameMetadata;
import com.selesse.steam.games.XmlGamesParser;
import com.selesse.steam.registry.RegistryPrettyPrint;
import com.selesse.steam.registry.SteamRegistry;
import com.selesse.steam.registry.implementation.RegistryParser;
import com.selesse.steam.registry.implementation.RegistryStore;
import com.selesse.steam.user.SteamAccountIdFinder;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameLoadingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameLoadingService.class);

    private final GameRegistries gameRegistries;
    private final SteamRegistry steamRegistry;
    private final SteamCrossplatformSyncConfig config;

    public GameLoadingService(SteamCrossplatformSyncConfig config) {
        this.config = config;
        if (config.getRemoteAppInfoUrl() != null) {
            gameRegistries = GameRegistries.buildWithRemoteFallback(config.getRemoteAppInfoUrl());
        } else {
            gameRegistries = GameRegistries.build();
        }
        steamRegistry = SteamRegistry.getInstance();
    }

    public SteamGame loadGame(long gameId) {
        FileBackedCache fileBackedCache = new FileBackedCacheBuilder()
                .setCacheLoadingCriteria(this::accurateEnoughGameCache)
                .setLoadingMechanism(() -> RegistryPrettyPrint.prettyPrint(gameRegistries.load(gameId)))
                .build();
        Path cachedRegistryStore = Path.of(config.getCacheDirectory().toString(), gameId + ".vdf");
        RegistryStore registryStore = RegistryParser.parse(fileBackedCache.getLines(cachedRegistryStore));
        SteamGameMetadata gameMetadata = steamRegistry.getGameMetadata(gameId);
        return new SteamGame(gameMetadata, registryStore);
    }

    public List<SteamGame> loadInstalledGames() {
        return steamRegistry.getInstalledAppIds().stream().map(this::loadGame).toList();
    }

    public List<SteamGame> fetchAllGamesOrLoadInstalledGames() {
        Optional<SteamAccountId> steamAccountIdMaybe = SteamAccountIdFinder.findIfPresent();
        return steamAccountIdMaybe.map(this::attemptToFetchPublicGamesList).orElse(loadInstalledGames());
    }

    private List<SteamGame> attemptToFetchPublicGamesList(SteamAccountId accountId) {
        Path cachedGameList = getCachedGameList(accountId);
        FileBackedCache fileBackedCache = new FileBackedCacheBuilder()
                .setCacheLoadingCriteria(this::accurateEnoughGameListCache)
                .setLoadingMechanism(() -> {
                    RemoteGameListFetcher remoteGameListFetcher = new RemoteGameListFetcher(accountId);
                    try {
                        return remoteGameListFetcher.getOutputFromRemote();
                    } catch (IOException | InterruptedException e) {
                        LOGGER.error("Unable to fetch remote game list", e);
                        return "";
                    }
                })
                .build();
        List<String> lines = fileBackedCache.getLines(cachedGameList);
        var appIdList = new XmlGamesParser().getAppIdList(accountId, String.join("\n", lines));
        return appIdList.stream().map(this::loadGame).toList();
    }

    private Path getCachedGameList(SteamAccountId accountId) {
        return Path.of(config.getCacheDirectory().toString(), accountId.to64Bit() + ".xml");
    }

    private boolean accurateEnoughGameCache(Path path) {
        return path.toFile().exists() && FileModified.getDaysSinceModification(path.toFile()) < 30;
    }

    private boolean accurateEnoughGameListCache(Path path) {
        return path.toFile().exists() && FileModified.getHoursSinceModification(path.toFile()) < 1;
    }
}
