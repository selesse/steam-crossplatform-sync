package com.selesse.steam.appcache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// The query behind readSome()/readOne(): parse only entries whose ID is in the requested set,
// skipping every other entry's bytes unread, and stopping as soon as every requested ID is found.
final class SomeApps implements AppQuery {
    private final Set<Long> remaining;
    private final Map<Long, App> results = new HashMap<>();

    SomeApps(Set<Long> targetAppIds) {
        this.remaining = new HashSet<>(targetAppIds);
    }

    @Override
    public boolean shouldParse(int appId) {
        return remaining.contains((long) appId);
    }

    @Override
    public void onParsed(int appId, App app) {
        results.put((long) appId, app);
        remaining.remove((long) appId);
    }

    @Override
    public boolean isDone() {
        return remaining.isEmpty();
    }

    Map<Long, App> results() {
        return results;
    }
}
