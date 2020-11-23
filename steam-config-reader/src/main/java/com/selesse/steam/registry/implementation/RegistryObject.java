package com.selesse.steam.registry.implementation;

import com.google.common.base.Splitter;
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

    public RegistryObject getObjectValueAsObject(String path) {
        return (RegistryObject) getObjectValue(path);
    }

    public RegistryString getObjectValueAsString(String path) {
        return (RegistryString) getObjectValue(path);
    }

    public RegistryValue getObjectValue(String path) {
        List<String> parts = Splitter.on("/").splitToList(path);

        RegistryObject currentObject = this;

        for (String part : parts) {
            RegistryValue registryValue = currentObject.get(part);
            if (registryValue instanceof RegistryObject) {
                currentObject = (RegistryObject) registryValue;
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
            if (registryValue instanceof RegistryObject) {
                currentObject = (RegistryObject) registryValue;
            }
        }

        return true;
    }
}
