package com.selesse.steam.registry;

import com.google.common.collect.Lists;
import com.selesse.steam.AppType;
import com.selesse.steam.SteamApp;
import com.selesse.steam.SteamAppLoader;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryParser;
import com.selesse.steam.registry.implementation.RegistryStore;
import com.selesse.steam.registry.implementation.RegistryString;
import com.selesse.steam.steamcmd.games.SteamGameMetadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public abstract class FileBasedRegistry extends SteamRegistry {
    private final RegistryStore registryStore;

    abstract Path registryPath();

    public FileBasedRegistry() {
        try {
            ensureRegistryPathExists();
            this.registryStore = RegistryParser.parse(Files.readAllLines(registryPath()));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void ensureRegistryPathExists() {
        if (!registryPath().toFile().isFile()) {
            throw new RuntimeException("Registry file does not exist in " + registryPath());
        }
    }

    public long getCurrentlyRunningAppId() {
        RegistryString value = registryStore.getObjectValueAsString("Registry/HKCU/Software/Valve/Steam/RunningAppID");
        return Long.parseLong(value.getValue());
    }

    public List<Long> getInstalledAppIds() {
        RegistryObject object = getAppsRegistryObject();
        return object.getKeys().stream().map(Long::valueOf).sorted().collect(Collectors.toList());
    }

    public List<SteamGameMetadata> getGameMetadata() {
        RegistryObject object = getAppsRegistryObject();
        List<SteamGameMetadata> steamGames = Lists.newArrayList();
        for (long gameId : object.getKeys().stream().map(Long::parseLong).collect(Collectors.toList())) {
            if (getRegistryObject(gameId).pathExists("installed")) {
                SteamApp steamApp = SteamAppLoader.load(gameId);

                if (steamApp.getType() == AppType.GAME) {
                    steamGames.add(getGameMetadata(gameId));
                }
            }
        }
        return steamGames;
    }

    public SteamGameMetadata getGameMetadata(Long gameId) {
        SteamApp steamApp = SteamAppLoader.load(gameId);
        String installedValue = getRegistryObject(gameId).getObjectValueAsString("installed").getValue();
        return new SteamGameMetadata(gameId, steamApp.getName(), installedValue.equals("1"));
    }

    private RegistryObject getAppsRegistryObject() {
        return registryStore.getObjectValueAsObject("Registry/HKCU/Software/Valve/Steam/apps");
    }

    private RegistryObject getRegistryObject(long gameId) {
        return getAppsRegistryObject().getObjectValueAsObject("" + gameId);
    }
}
