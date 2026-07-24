package com.selesse.steam;

import com.selesse.steam.appcache.*;
import com.selesse.steam.registry.RegistryNotFoundException;
import com.selesse.steam.registry.SteamRegistry;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryStore;
import com.selesse.steam.registry.implementation.RegistryString;
import java.nio.file.Path;

public class SteamAppLoader {
    public static SteamApp load(long gameId) {
        return load(SteamRegistry.getInstance().getAppCachePath(), gameId);
    }

    public static SteamApp load(Path appCachePath, long gameId) {
        App rawApp = new AppCacheReader().loadOne(appCachePath, gameId).orElseThrow(RegistryNotFoundException::new);
        return toSteamApp(rawApp);
    }

    public static SteamApp findByName(String name) {
        return findByName(SteamRegistry.getInstance().getAppCachePath(), name);
    }

    public static SteamApp findByName(Path appCachePath, String name) {
        App rawApp = new AppCacheReader()
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
        RegistryObject registryObject = convert(app.vdfObject());
        return registryObject.pathExists("common/name")
                && registryObject
                        .getObjectValueAsString("common/name")
                        .getValue()
                        .equals(name);
    }

    private static SteamApp toSteamApp(App rawApp) {
        return new SteamApp(new RegistryStore(convert(rawApp.vdfObject())));
    }
}
