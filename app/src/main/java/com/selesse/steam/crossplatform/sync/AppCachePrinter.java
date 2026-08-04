package com.selesse.steam.crossplatform.sync;

import com.selesse.steam.AppCacheReader;
import com.selesse.steam.SteamAppLoader;
import com.selesse.steam.SteamInstall;
import com.selesse.steam.appcache.App;
import com.selesse.steam.appcache.AppCache;
import com.selesse.steam.registry.RegistryPrettyPrint;
import com.selesse.steam.registry.implementation.RegistryString;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppCachePrinter {
    private final AppCacheReader appCacheReader;

    public AppCachePrinter(SteamInstall install) {
        this.appCacheReader = new AppCacheReader(install.registry());
    }

    public void run() {
        AppCache appCache = appCacheReader.load();
        List<App> apps = new ArrayList<>(appCache.getApps());
        apps.sort(Comparator.comparingInt(App::appId));
        apps.forEach(this::printApp);
    }

    public void run(Long... appIds) {
        Map<Long, App> apps = appCacheReader.loadSome(Set.of(appIds));
        for (Long appId : appIds) {
            App app = apps.get(appId);
            if (app == null) {
                System.out.println("No app found in the app cache for ID " + appId);
                continue;
            }
            printApp(app);
        }
    }

    public void listIds() {
        AppCache appCache = appCacheReader.load();
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
        return SteamAppLoader.convert(app.vdfObject())
                .findString("common/name")
                .map(RegistryString::getValue)
                .orElse("");
    }

    private void printApp(App app) {
        System.out.println("App ID: " + app.appId());
        System.out.println(RegistryPrettyPrint.prettyPrint(SteamAppLoader.convert(app.vdfObject())));
    }
}
