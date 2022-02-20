package com.selesse.steam.registry.mac;

import com.google.common.collect.Lists;
import com.selesse.steam.AppListFetcher;
import com.selesse.steam.applist.SteamNameAndId;
import com.selesse.steam.applist.SteamAppList;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryParser;
import com.selesse.steam.registry.implementation.RegistryStore;
import com.selesse.steam.registry.implementation.RegistryString;
import com.selesse.steam.steamcmd.games.SteamGameMetadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
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
            RegistryObject registryObject = object.getObjectValueAsObject(gameIdString);
            if (registryObject.pathExists("installed")) {
                RegistryString nameValue = registryObject.getObjectValueAsString("name");
                String name = Optional.ofNullable(nameValue).map(RegistryString::getValue).orElse("");
                RegistryString installedValue = registryObject.getObjectValueAsString("installed");
                boolean installed = installedValue.getValue().equals("1");
                long gameId = Long.parseLong(gameIdString);
                SteamGameMetadata steamGameMetadata = new SteamGameMetadata(gameId, name, installed);
                steamGames.add(steamGameMetadata);
            }
        }
        return steamGames;
    }

    public SteamGameMetadata getGameMetadata(Long gameId) {
        RegistryObject object = registryStore.getObjectValueAsObject("Registry/HKCU/Software/Valve/Steam/apps");
        RegistryObject registryObject = object.getObjectValueAsObject("" + gameId);
        if (registryObject != null) {
            RegistryString nameValue = registryObject.getObjectValueAsString("name");
            String name = Optional.ofNullable(nameValue).map(RegistryString::getValue).orElse("");
            RegistryString installedValue = registryObject.getObjectValueAsString("installed");
            boolean installed = installedValue.getValue().equals("1");
            return new SteamGameMetadata(gameId, name, installed);
        } else {
            SteamAppList appList = AppListFetcher.fetchAppList();
            SteamNameAndId app = appList.getAppById(gameId);
            return new SteamGameMetadata(gameId, app.name, false);
        }
    }
}
