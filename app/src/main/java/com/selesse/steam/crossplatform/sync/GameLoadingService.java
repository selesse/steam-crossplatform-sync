package com.selesse.steam.crossplatform.sync;

import com.selesse.files.RuntimeExceptionFiles;
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
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
        RegistryStore registryStore = null;

        Path cachedRegistryStore = Path.of(config.getCacheDirectory().toString(), gameId + ".vdf");
        if (accurateEnoughGameCache(cachedRegistryStore)) {
            List<String> lines = RuntimeExceptionFiles.readAllLines(cachedRegistryStore);
            if (lines.size() > 3) {
                registryStore = RegistryParser.parse(lines);
            }
        }
        if (registryStore == null) {
            registryStore = gameRegistries.load(gameId);
            RuntimeExceptionFiles.writeString(cachedRegistryStore, RegistryPrettyPrint.prettyPrint(registryStore));
        }
        SteamGameMetadata gameMetadata = steamRegistry.getGameMetadata(gameId);
        return new SteamGame(gameMetadata, registryStore);
    }

    public List<SteamGame> loadInstalledGames() {
        return steamRegistry.getInstalledAppIds().stream().map(this::loadGame).toList();
    }

    public List<SteamGame> fetchAllGamesOrLoadInstalledGames() {
        Optional<SteamAccountId> steamAccountIdMaybe = SteamAccountIdFinder.findIfPresent();
        var steamGamesMaybe = steamAccountIdMaybe.map(this::attemptToFetchPublicGamesList);
        return steamGamesMaybe.orElse(loadInstalledGames());
    }

    private List<SteamGame> attemptToFetchPublicGamesList(SteamAccountId accountId) {
        List<Long> appIdList;
        Path cachedGameList = getCachedGameList(accountId);
        if (accurateEnoughGameListCache(cachedGameList)) {
            List<String> xmlFile = RuntimeExceptionFiles.readAllLines(cachedGameList);
            appIdList = new XmlGamesParser().getAppIdList(accountId, String.join("\n", xmlFile));
        } else {
            LOGGER.info("Fetching games.xml for steam ID {} to get games list", accountId.to64Bit());
            RemoteGameListFetcher remoteGameListFetcher = new RemoteGameListFetcher(accountId);
            appIdList = remoteGameListFetcher.fetchGameIdList();
            RuntimeExceptionFiles.writeString(cachedGameList, remoteGameListFetcher.getOutput());
        }
        return appIdList.stream().map(this::loadGame).toList();
    }

    private Path getCachedGameList(SteamAccountId accountId) {
        return Path.of(config.getCacheDirectory().toString(), accountId.to64Bit() + ".xml");
    }

    private boolean accurateEnoughGameCache(Path cachedRegistryStore) {
        long lastModified = cachedRegistryStore.toFile().lastModified();
        return cachedRegistryStore.toFile().exists() && getDaysSinceModification(lastModified) < 30;
    }

    private boolean accurateEnoughGameListCache(Path cachedRegistryStore) {
        long lastModified = cachedRegistryStore.toFile().lastModified();
        return cachedRegistryStore.toFile().exists() && getHoursSinceModification(lastModified) < 1;
    }

    private long getDaysSinceModification(long lastModified) {
        LocalDateTime localLastModified =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified), ZoneId.systemDefault());
        return Math.abs(ChronoUnit.DAYS.between(LocalDateTime.now(), localLastModified));
    }

    private long getHoursSinceModification(long lastModified) {
        LocalDateTime localLastModified =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified), ZoneId.systemDefault());
        return Math.abs(ChronoUnit.HOURS.between(LocalDateTime.now(), localLastModified));
    }
}
