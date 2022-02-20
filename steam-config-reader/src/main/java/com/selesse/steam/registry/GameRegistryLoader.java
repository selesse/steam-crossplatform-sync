package com.selesse.steam.registry;

import com.selesse.steam.registry.implementation.RegistryStore;

public interface GameRegistryLoader {
    RegistryStore load(long gameId) throws RegistryNotFoundException;
}
