package com.selesse.steam.games;

import com.selesse.steam.AppType;
import com.selesse.steam.SteamAppLoader;
import java.util.List;

public class InstalledGameFinderService {
    private final List<InstalledGameFetcher> finders;

    public InstalledGameFinderService() {
        this.finders = List.of(new AppManifestInstalledGameFinder());
    }

    public List<Long> find() {
        for (InstalledGameFetcher finder : finders) {
            var ids = finder.fetch().stream().filter(this::isAGame).toList();
            if (!ids.isEmpty()) {
                return ids;
            }
        }
        throw new IllegalStateException("Error - could not find any installed games");
    }

    private boolean isAGame(Long gameId) {
        return SteamAppLoader.load(gameId).getType() == AppType.GAME;
    }
}
