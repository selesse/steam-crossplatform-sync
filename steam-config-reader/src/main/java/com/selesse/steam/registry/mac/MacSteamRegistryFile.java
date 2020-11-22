package com.selesse.steam.registry.mac;

import com.google.common.collect.Lists;
import com.selesse.steam.registry.implementation.*;
import com.selesse.steam.steamcmd.games.SteamGameMetadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class MacSteamRegistryFile {
    private final RegistryStore registryStore;

    public MacSteamRegistryFile(Path registryFilePath) {
        try {
            List<String> lines = Files.readAllLines(registryFilePath);
            this.registryStore = RegistryParser.parse(lines);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read registry file " + registryFilePath.toAbsolutePath());
        }
    }

    public Long getCurrentlyRunningAppId() {
        RegistryString value = registryStore.getObjectValueAsString("Registry/HKCU/Software/Valve/Steam/RunningAppID");
        return Long.parseLong(value.getValue());
    }

    public List<Long> getInstalledAppIds() {
        RegistryObject object = registryStore.getObjectValueAsObject("Registry/HKCU/Software/Valve/Steam/apps");
        return object.getKeys().stream().map(Long::valueOf).sorted().collect(Collectors.toList());
    }

    public List<SteamGameMetadata> getGameMetadata() {
        RegistryObject object = registryStore.getObjectValueAsObject("Registry/HKCU/Software/Valve/Steam/apps");
        List<SteamGameMetadata> steamGames = Lists.newArrayList();
        for (String gameIdString : object.getKeys()) {
            RegistryValue registryObject = object.get(gameIdString);
            if (registryObject instanceof RegistryObject) {
                RegistryObject registryValue = (RegistryObject) registryObject;
                if (registryValue.getKeys().contains("name")) {
                    RegistryValue nameValue = registryValue.get("name");
                    String name = ((RegistryString) nameValue).getValue();
                    RegistryValue installedValue = registryValue.get("installed");
                    boolean installed = ((RegistryString) installedValue).getValue().equals("1");
                    long gameId = Long.parseLong(gameIdString);
                    SteamGameMetadata steamGameMetadata = new SteamGameMetadata(gameId, name, installed);
                    steamGames.add(steamGameMetadata);
                }
            }
        }
        return steamGames;
    }
}
