package com.selesse.steam.registry.implementation;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RegistryObject extends RegistryValue {
    private final Map<String, RegistryValue> values;

    public RegistryObject() {
        this.values = new LinkedHashMap<>();
    }

    public RegistryValue get(String part) {
        // Deal with registry lookups in case insensitive ways (e.g. for OS X)
        if (values.containsKey(part.toLowerCase())) {
            return values.get(part.toLowerCase());
        }
        return values.get(part);
    }

    public List<String> getKeys() {
        return Lists.newArrayList(values.keySet());
    }

    public void put(String key, RegistryValue value) {
        values.put(key, value);
    }

    public RegistryObject getObjectValueAsObject(String path) {
        return (RegistryObject) getObjectValue(path);
    }

    public RegistryString getObjectValueAsString(String path) {
        return (RegistryString) getObjectValue(path);
    }

    public RegistryValue getObjectValue(String path) {
        if (values.containsKey(path)) {
            return values.get(path);
        }
        List<String> parts = Splitter.on("/").splitToList(path);

        RegistryObject currentObject = this;

        for (String part : parts) {
            RegistryValue registryValue = currentObject.get(part);
            if (registryValue instanceof RegistryObject registryObject) {
                currentObject = registryObject;
            } else {
                return registryValue;
            }
        }

        return currentObject;
    }

    public boolean pathExists(String path) {
        List<String> parts = Splitter.on("/").splitToList(path);

        RegistryObject currentObject = this;

        for (String part : parts) {
            RegistryValue registryValue = currentObject.get(part);
            if (registryValue == null) {
                return false;
            }
            if (registryValue instanceof RegistryObject registryObject) {
                currentObject = registryObject;
            }
        }

        return true;
    }
}
