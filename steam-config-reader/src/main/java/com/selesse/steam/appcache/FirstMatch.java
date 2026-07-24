package com.selesse.steam.appcache;

import java.util.function.Predicate;

// The query behind readFirst(): parse entries in file order until one satisfies the given
// predicate, then stop. Unlike SomeApps, a match here isn't decidable from the appId header
// alone, so every entry has to be parsed to test it.
final class FirstMatch implements AppQuery {
    private final Predicate<App> predicate;
    private App result;

    FirstMatch(Predicate<App> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean shouldParse(int appId) {
        return true;
    }

    @Override
    public void onParsed(int appId, App app) {
        if (result == null && predicate.test(app)) {
            result = app;
        }
    }

    @Override
    public boolean isDone() {
        return result != null;
    }

    App result() {
        return result;
    }
}
