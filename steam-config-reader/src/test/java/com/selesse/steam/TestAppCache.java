package com.selesse.steam;

import com.selesse.os.Resources;
import com.selesse.steam.appcache.AppCache;

public class TestAppCache {
    private static AppCache appCache;

    public static void setup() {
        if (appCache == null) {
            appCache = new AppCacheReader().load(Resources.getResource("appinfo-pre-dec-2022.vdf"));
        }
        SteamAppLoader.primeAppCache(appCache);
    }
}
