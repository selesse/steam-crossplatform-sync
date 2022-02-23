package com.selesse.steam;

import com.selesse.steam.appcache.*;
import com.selesse.steam.registry.RegistryNotFoundException;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryStore;
import com.selesse.steam.registry.implementation.RegistryString;

import java.util.Collection;

public class SteamAppLoader {
    private static AppCache appCache;

    public static void primeAppCache(AppCache appCache) {
        SteamAppLoader.appCache = appCache;
    }

    public static SteamApp load(Long gameId) {
        App rawApp = loadAppCache().getById(gameId);
        if (rawApp == null) {
            throw new RegistryNotFoundException();
        }

        RegistryObject rootRegistry = convert(rawApp.vdfObject());
        RegistryStore registryStore = new RegistryStore(rootRegistry);

        return new SteamApp(registryStore);
    }

    public static SteamApp findByName(String name) {
        Collection<App> apps = loadAppCache().getApps();

        RegistryObject registryObject = apps.stream()
                .map(steamApp -> convert(steamApp.vdfObject()))
                .filter(registry -> registry.pathExists("common/name") && registry.getObjectValueAsString("common/name").getValue().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find app named " + name));

        return new SteamApp(new RegistryStore(registryObject));
    }

    private static RegistryObject convert(VdfObject object) {
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

    private static AppCache loadAppCache() {
        if (appCache == null) {
            appCache = new AppCacheReader().load();
        }
        return appCache;
    }

}
