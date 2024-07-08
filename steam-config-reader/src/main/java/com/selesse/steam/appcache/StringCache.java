package com.selesse.steam.appcache;

import java.util.ArrayList;
import java.util.List;

public class StringCache {
    private final List<String> cache;

    public StringCache() {
        this.cache = new ArrayList<>();
    }

    public void append(String string) {
        cache.add(string);
    }

    public int size() {
        return cache.size();
    }

    public String get(int index) {
        return cache.get(index);
    }
}
