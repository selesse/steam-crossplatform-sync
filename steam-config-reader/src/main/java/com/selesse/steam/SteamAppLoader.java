package com.selesse.steam;

import com.selesse.steam.appcache.*;
import com.selesse.steam.registry.RegistryNotFoundException;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryString;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SteamAppLoader {
    private final SteamInstall install;
    private final AppCacheReader appCacheReader;

    public SteamAppLoader(SteamInstall install) {
        this.install = install;
        this.appCacheReader = new AppCacheReader(install.registry());
    }

    public SteamApp load(long gameId) {
        return load(install.registry().getAppCachePath(), gameId);
    }

    public SteamApp load(Path appCachePath, long gameId) {
        App rawApp = appCacheReader.loadOne(appCachePath, gameId).orElseThrow(RegistryNotFoundException::new);
        return toSteamApp(rawApp);
    }

    public List<SteamApp> loadSome(List<Long> gameIds) {
        return loadSome(install.registry().getAppCachePath(), gameIds);
    }

    /** Loads every id in a single pass over the app cache, rather than one pass per id. */
    public List<SteamApp> loadSome(Path appCachePath, List<Long> gameIds) {
        Map<Long, App> rawApps = appCacheReader.loadSome(appCachePath, Set.copyOf(gameIds));
        return gameIds.stream()
                .map(gameId -> {
                    App rawApp = rawApps.get(gameId);
                    if (rawApp == null) {
                        throw new RegistryNotFoundException();
                    }
                    return toSteamApp(rawApp);
                })
                .toList();
    }

    public SteamApp findByName(Path appCachePath, String name) {
        App rawApp = appCacheReader
                .findFirst(appCachePath, app -> nameMatches(app, name))
                .orElseThrow(() -> new RuntimeException("Could not find app named " + name));
        return toSteamApp(rawApp);
    }

    public static RegistryObject convert(VdfObject object) {
        RegistryObject registryObject = new RegistryObject();

        for (Object value : object.getValues()) {
            if (value instanceof VdfObject nestedVdfObject) {
                RegistryObject nestedObject = convert(nestedVdfObject);
                registryObject.put(nestedVdfObject.getName(), nestedObject);
            } else if (value instanceof VdfString stringObject) {
                RegistryString registryString = new RegistryString(stringObject.name(), "" + stringObject.value());
                registryObject.put(registryString.getName(), registryString);
            } else if (value instanceof VdfInteger intObject) {
                RegistryString registryString = new RegistryString(intObject.name(), "" + intObject.value());
                registryObject.put(intObject.name(), registryString);
            }
        }

        return registryObject;
    }

    private static boolean nameMatches(App app, String name) {
        return convert(app.vdfObject())
                .findString("common/name")
                .map(appName -> appName.getValue().equals(name))
                .orElse(false);
    }

    private SteamApp toSteamApp(App rawApp) {
        return new SteamApp(convert(rawApp.vdfObject()), install);
    }
}
