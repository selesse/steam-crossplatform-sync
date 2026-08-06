package com.selesse.steam.games;

import com.selesse.steam.AppCacheReader;
import com.selesse.steam.AppType;
import com.selesse.steam.SteamAppLoader;
import com.selesse.steam.SteamInstall;
import com.selesse.steam.appcache.App;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.VisibleForTesting;

public class InstalledGameFinderService {
    private final List<InstalledGameFetcher> finders;
    private final AppCacheReader appCacheReader;

    public InstalledGameFinderService(SteamInstall install) {
        this(List.of(new AppManifestInstalledGameFinder(install.registry())), new AppCacheReader(install.registry()));
    }

    @VisibleForTesting
    InstalledGameFinderService(List<InstalledGameFetcher> finders, AppCacheReader appCacheReader) {
        this.finders = finders;
        this.appCacheReader = appCacheReader;
    }

    public List<Long> find() {
        for (InstalledGameFetcher finder : finders) {
            List<Long> candidateIds = finder.fetch();
            Map<Long, App> apps = appCacheReader.loadSome(Set.copyOf(candidateIds));
            var ids = candidateIds.stream().filter(id -> isAGame(apps.get(id))).toList();
            if (!ids.isEmpty()) {
                return ids;
            }
        }
        throw new IllegalStateException("Error - could not find any installed games");
    }

    // app is null when its manifest exists but it isn't in the app cache (e.g. not yet synced) -
    // treat that as "not a game" rather than failing the whole scan over one entry.
    private boolean isAGame(App app) {
        if (app == null) {
            return false;
        }
        var registryObject = SteamAppLoader.convert(app.vdfObject());
        return AppType.fromString(registryObject.getObjectValueAsString("common/type")) == AppType.GAME;
    }
}
