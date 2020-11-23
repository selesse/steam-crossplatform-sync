package com.selesse.steam.registry.implementation;

public class RegistryStore {
    private final RegistryObject rootObject;

    public RegistryStore(RegistryObject rootObject) {
        this.rootObject = rootObject;
    }

    public RegistryObject getObjectValueAsObject(String path) {
        return rootObject.getObjectValueAsObject(path);
    }

    public RegistryString getObjectValueAsString(String path) {
        return rootObject.getObjectValueAsString(path);
    }

    public RegistryValue getObjectValue(String path) {
        return rootObject.getObjectValue(path);
    }

    public boolean pathExists(String path) {
        return rootObject.pathExists(path);
    }
}
