package com.selesse.steam.crossplatform.sync;

import com.selesse.steam.AppCacheReader;
import com.selesse.steam.SteamAppLoader;
import com.selesse.steam.appcache.App;
import com.selesse.steam.appcache.AppCache;
import com.selesse.steam.registry.RegistryPrettyPrint;
import com.selesse.steam.registry.implementation.RegistryObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AppCachePrinter {
    public void run() {
        AppCache appCache = new AppCacheReader().load();
        List<App> apps = new ArrayList<>(appCache.getApps());
        apps.sort(Comparator.comparingInt(App::appId));
        apps.forEach(this::printApp);
    }

    public void run(Long... appIds) {
        AppCache appCache = new AppCacheReader().load();
        for (Long appId : appIds) {
            App app = appCache.getById(appId);
            if (app == null) {
                System.out.println("No app found in the app cache for ID " + appId);
                continue;
            }
            printApp(app);
        }
    }

    public void listIds() {
        AppCache appCache = new AppCacheReader().load();
        List<App> apps = new ArrayList<>(appCache.getApps());
        apps.sort(Comparator.comparing(this::nameOrEmpty, String.CASE_INSENSITIVE_ORDER));
        for (App app : apps) {
            String name = nameOrEmpty(app);
            if (!name.isEmpty()) {
                System.out.println(app.appId() + "\t" + name);
            }
        }
    }

    private String nameOrEmpty(App app) {
        RegistryObject registryObject = SteamAppLoader.convert(app.vdfObject());
        return registryObject.pathExists("common/name")
                ? registryObject.getObjectValueAsString("common/name").getValue()
                : "";
    }

    private void printApp(App app) {
        System.out.println("App ID: " + app.appId());
        System.out.println(RegistryPrettyPrint.prettyPrint(SteamAppLoader.convert(app.vdfObject())));
    }
}
