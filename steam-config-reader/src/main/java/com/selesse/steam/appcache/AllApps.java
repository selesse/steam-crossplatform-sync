package com.selesse.steam.appcache;

// The query behind read(): parse every entry, never stop early.
final class AllApps implements AppQuery {
    private final AppCache appCache;

    AllApps(AppCache appCache) {
        this.appCache = appCache;
    }

    @Override
    public boolean shouldParse(int appId) {
        return true;
    }

    @Override
    public void onParsed(int appId, App app) {
        appCache.add(app);
    }

    @Override
    public boolean isDone() {
        return false;
    }
}
