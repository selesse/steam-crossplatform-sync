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
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameLoadingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameLoadingService.class);

    private final GameRegistries gameRegistries;
    private final SteamCrossplatformSyncConfig config;

    public GameLoadingService(SteamCrossplatformSyncConfig config) {
        this.config = config;
        if (config.getRemoteAppInfoUrl() != null) {
            gameRegistries = GameRegistries.buildWithRemoteFallback(config.getRemoteAppInfoUrl());
        } else {
            gameRegistries = GameRegistries.build();
        }
    }

    public SteamGame loadGame(long gameId) {
        FileBackedCache fileBackedCache = new FileBackedCacheBuilder()
                .setCacheLoadingCriteria(this::accurateEnoughGameCache)
                .setLoadingMechanism(() -> RegistryPrettyPrint.prettyPrint(gameRegistries.load(gameId)))
                .setSuccessfulLoadCriteria(x -> x.size() > 3)
                .build();
        Path cachedRegistryStore = Path.of(config.getCacheDirectory().toString(), gameId + ".vdf");
        RegistryStore registryStore = RegistryParser.parse(fileBackedCache.getLines(cachedRegistryStore));
        SteamGameMetadata gameMetadata = SteamRegistry.getInstance().getGameMetadata(gameId);
        return new SteamGame(gameMetadata, registryStore);
    }

    public List<SteamGame> loadInstalledGames() {
        return SteamRegistry.getInstance().getInstalledAppIds().stream()
                .map(this::loadGame)
                .toList();
    }

    public List<SteamGame> fetchAllGamesOrLoadInstalledGames() {
        Optional<SteamAccountId> steamAccountIdMaybe = SteamAccountIdFinder.findIfPresent();
        return steamAccountIdMaybe.flatMap(this::attemptToFetchPublicGamesList).orElse(loadInstalledGames());
    }

    private Optional<List<SteamGame>> attemptToFetchPublicGamesList(SteamAccountId accountId) {
        Path cachedGameList = Path.of(config.getCacheDirectory().toString(), accountId.to64Bit() + ".xml");
        Function<List<String>, Boolean> validGameList =
                lines -> !String.join("\n", lines).trim().isEmpty();
        FileBackedCache fileBackedCache = new FileBackedCacheBuilder()
                .setCacheLoadingCriteria(this::accurateEnoughGameListCache)
                .setLoadingMechanism(() -> {
                    RemoteGameListFetcher remoteGameListFetcher = new RemoteGameListFetcher(accountId);
                    try {
                        return remoteGameListFetcher.getOutputFromRemote();
                    } catch (IOException | InterruptedException e) {
                        LOGGER.warn("Unable to fetch remote game list");
                        return "";
                    }
                })
                .setSuccessfulLoadCriteria(validGameList)
                .build();
        List<String> lines = fileBackedCache.getLines(cachedGameList);
        if (!validGameList.apply(lines)) {
            return Optional.empty();
        }
        var appIdList = new XmlGamesParser().getAppIdList(accountId, String.join("\n", lines));
        return Optional.of(appIdList.stream().map(this::loadGame).toList());
    }

    private boolean accurateEnoughGameCache(Path path) {
        return path.toFile().exists() && FileModified.getDaysSinceModification(path.toFile()) < 30;
    }

    private boolean accurateEnoughGameListCache(Path path) {
        return path.toFile().exists() && FileModified.getHoursSinceModification(path.toFile()) < 1;
    }
}
