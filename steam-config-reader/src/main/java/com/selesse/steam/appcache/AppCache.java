package com.selesse.steam.appcache;

import java.util.HashMap;
import java.util.Map;

public class AppCache {
    private final Map<Integer, App> appMap;

    public AppCache() {
        this.appMap = new HashMap<>();
    }

    public void add(App app) {
        appMap.put(app.getAppId(), app);
    }

    public App getById(int id) {
        return appMap.get(id);
    }

    public int size() {
        return appMap.size();
    }
}
