package com.selesse.steam.registry.implementation;

import com.google.common.base.Splitter;

import java.util.List;

public class RegistryStore {
    private final RegistryObject rootObject;

    public RegistryStore(RegistryObject rootObject) {
        this.rootObject = rootObject;
    }

    public RegistryObject getObjectValue(String path) {
        List<String> parts = Splitter.on("/").splitToList(path);

        RegistryObject currentObject = rootObject;

        for (String part : parts) {
            RegistryValue registryValue = currentObject.get(part);
            if (registryValue instanceof RegistryObject) {
                currentObject = (RegistryObject) registryValue;
            }
        }

        return currentObject;
    }
}
