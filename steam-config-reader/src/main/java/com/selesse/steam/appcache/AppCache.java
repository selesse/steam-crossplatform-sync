package com.selesse.steam.appcache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AppCache {
    private final Map<Long, App> appMap;

    public AppCache() {
        this.appMap = new HashMap<>();
    }

    public void add(App app) {
        appMap.put((long) app.appId(), app);
    }

    public App getById(long id) {
        return appMap.get(id);
    }

    public int size() {
        return appMap.size();
    }

    public Collection<App> getApps() {
        return appMap.values();
    }
}
