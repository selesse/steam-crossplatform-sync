package com.selesse.steam;

import com.selesse.steam.appcache.*;
import com.selesse.steam.registry.RegistryNotFoundException;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryStore;
import com.selesse.steam.registry.implementation.RegistryString;

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

        RegistryObject rootRegistry = convert(rawApp.getVdfObject());
        RegistryStore registryStore = new RegistryStore(rootRegistry);

        return new SteamApp(registryStore);
    }

    private static RegistryObject convert(VdfObject object) {
        RegistryObject registryObject = new RegistryObject();

        for (Object value : object.getValues()) {
            if (value instanceof VdfObject) {
                VdfObject nestedVdfObject = (VdfObject) value;
                RegistryObject nestedObject = convert(nestedVdfObject);
                registryObject.put(nestedVdfObject.getName(), nestedObject);
            } else if (value instanceof VdfString) {
                VdfString stringObject = (VdfString) value;
                RegistryString registryString = new RegistryString(stringObject.getName(), "" + stringObject.getValue());
                registryObject.put(registryString.getName(), registryString);
            } else if (value instanceof VdfInteger) {
                VdfInteger intObject = (VdfInteger) value;
                RegistryString registryString = new RegistryString(intObject.getName(), "" + intObject.getValue());
                registryObject.put(intObject.getName(), registryString);
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
