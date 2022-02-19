package com.selesse.steam;

import com.selesse.steam.registry.implementation.RegistryStore;
import com.selesse.steam.registry.implementation.RegistryString;

public class SteamApp {
    private RegistryStore registryStore;

    public SteamApp(RegistryStore registryStore) {
        this.registryStore = registryStore;
    }

    public RegistryStore getRegistryStore() {
        return registryStore;
    }

    public AppType getType() {
        RegistryString objectValueAsString = registryStore.getObjectValueAsString("common/type");
        return AppType.fromString(objectValueAsString);
    }
}
