package com.selesse.steam.games.saves;

import com.selesse.steam.registry.implementation.RegistryObject;

public class SaveFileObject {
    private final RegistryObject object;

    public SaveFileObject(RegistryObject object) {
        this.object = object;
    }

    public boolean hasRoot() {
        return object.pathExists("root");
    }

    public String getRoot() {
        return object.getObjectValueAsString("root").getValue();
    }

    public boolean hasPath() {
        return object.pathExists("root");
    }

    public String getPath() {
        return object.getObjectValueAsString("path").getValue();
    }

    public boolean hasPattern() {
        return object.pathExists("pattern");
    }

    public String getPattern() {
        return object.getObjectValueAsString("pattern").getValue();
    }

    public boolean hasRecursive() {
        return object.pathExists("recursive");
    }

    public boolean isRecursive() {
        return object.getObjectValueAsString("recursive").getValue().equals("1");
    }
}
