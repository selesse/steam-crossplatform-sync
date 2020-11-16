package com.selesse.steam.registry.mac;

import com.selesse.steam.registry.implementation.*;

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
}
