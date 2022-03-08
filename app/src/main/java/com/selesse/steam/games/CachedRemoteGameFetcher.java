package com.selesse.steam.games;

import com.selesse.caches.FileBackedCache;
import com.selesse.caches.FileBackedCacheBuilder;
import com.selesse.files.FileModified;
import com.selesse.steam.SteamAccountId;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedRemoteGameFetcher implements InstalledGameFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedRemoteGameFetcher.class);

    private final SteamCrossplatformSyncConfig config;
    private final SteamAccountId accountId;

    public CachedRemoteGameFetcher(SteamCrossplatformSyncConfig config, SteamAccountId accountId) {
        this.config = config;
        this.accountId = accountId;
    }

    @Override
    public List<Long> fetch() {
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
            return new ArrayList<>();
        }
        return new XmlGamesParser().getAppIdList(accountId, String.join("\n", lines));
    }

    private boolean accurateEnoughGameListCache(Path path) {
        return path.toFile().exists() && FileModified.getHoursSinceModification(path.toFile()) < 1;
    }
}
