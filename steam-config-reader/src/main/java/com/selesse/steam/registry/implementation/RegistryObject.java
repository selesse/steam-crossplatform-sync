package com.selesse.steam.registry.implementation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class RegistryObject extends RegistryValue {
    private final Map<String, RegistryValue> values;

    public RegistryObject() {
        this.values = Maps.newHashMap();
    }

    public RegistryValue get(String part) {
        return values.get(part);
    }

    public List<String> getKeys() {
        return Lists.newArrayList(values.keySet());
    }

    public void put(String key, RegistryValue value) {
        values.put(key, value);
    }
}
