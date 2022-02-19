package com.selesse.steam;

import com.selesse.steam.registry.implementation.RegistryString;

public enum AppType {
    GAME("game"),
    CONFIG("config"),
    DLC("dlc"),
    TOOL("tool"),
    MUSIC("music"),
    DEMO("demo"),
    BETA("beta"),
    APPLICATION("application"),
    GUIDE("guide"),
    UNKNOWN("");

    private final String stringValue;

    AppType(String stringValue) {
        this.stringValue = stringValue;
    }

    public static AppType fromString(RegistryString registryString) {
        if (registryString == null) {
            return UNKNOWN;
        }
        for (AppType value : values()) {
            if (value.stringValue.equals(registryString.getValue().toLowerCase())) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown app type " + registryString.getValue());
    }
}
