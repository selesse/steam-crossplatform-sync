package com.selesse.steam.registry;

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
        return object.getKeys().stream().map(Long::valueOf)
                .filter(this::isInstalled).sorted().collect(Collectors.toList());
    }

    public List<SteamGameMetadata> getGameMetadata() {
        return getInstalledAppIds().stream()
                .map(this::getGameMetadata)
                .filter(gameMetadata -> SteamAppLoader.load(gameMetadata.getGameId()).getType() == AppType.GAME)
                .collect(Collectors.toList());
    }

    public SteamGameMetadata getGameMetadata(Long gameId) {
        SteamApp steamApp = SteamAppLoader.load(gameId);
        return new SteamGameMetadata(gameId, steamApp.getName(), getInstalledAppIds().contains(gameId));
    }

    private RegistryObject getAppsRegistryObject() {
        return registryStore.getObjectValueAsObject("Registry/HKCU/Software/Valve/Steam/apps");
    }

    private RegistryObject getRegistryObject(long gameId) {
        return getAppsRegistryObject().getObjectValueAsObject("" + gameId);
    }

    private boolean isInstalled(Long gameId) {
        RegistryObject registryObject = getRegistryObject(gameId);
        return registryObject.pathExists("installed") &&
                registryObject.getObjectValueAsString("installed").getValue().equals("1");
    }
}
