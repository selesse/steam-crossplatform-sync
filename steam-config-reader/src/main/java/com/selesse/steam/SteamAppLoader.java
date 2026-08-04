package com.selesse.steam;

import com.selesse.steam.appcache.*;
import com.selesse.steam.registry.RegistryNotFoundException;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryString;
import java.nio.file.Path;

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

    public SteamApp findByName(String name) {
        return findByName(install.registry().getAppCachePath(), name);
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
        RegistryObject registryObject = convert(app.vdfObject());
        return registryObject.pathExists("common/name")
                && registryObject
                        .getObjectValueAsString("common/name")
                        .getValue()
                        .equals(name);
    }

    private SteamApp toSteamApp(App rawApp) {
        return new SteamApp(convert(rawApp.vdfObject()), install);
    }
}
