package com.selesse.steam.registry.implementation;

public class RegistryString extends RegistryValue {
    private final String name;
    private final String value;

    public RegistryString(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
