package com.selesse.steam.appcache;

import com.selesse.steam.SteamAppLoader;
import com.selesse.steam.registry.GameRegistryLoader;
import com.selesse.steam.registry.RegistryNotFoundException;
import com.selesse.steam.registry.implementation.RegistryStore;

public class AppCacheRegistryLoader implements GameRegistryLoader  {
    @Override
    public RegistryStore load(long gameId) throws RegistryNotFoundException {
        return SteamAppLoader.load(gameId).getRegistryStore();
    }
}
