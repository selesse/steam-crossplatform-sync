package com.selesse.steam.registry.mac;

import com.google.common.base.Splitter;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryParser;
import com.selesse.steam.registry.implementation.RegistryStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class MacSteamRegistryFile {
    private final List<String> registryLines;

    public MacSteamRegistryFile(Path registryFilePath) {
        try {
            this.registryLines = Files.readAllLines(registryFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read registry file " + registryFilePath.toAbsolutePath());
        }
    }

    public Long getCurrentlyRunningAppId() {
        return registryLines.stream()
                .filter(line -> line.contains("RunningAppID"))
                .map(line -> {
                    List<String> partitions = Splitter.on("\"").splitToList(line);
                    return Long.valueOf(partitions.get(partitions.size() - 2));
                })
                .findFirst()
                .orElseThrow();
    }

    public List<Long> getInstalledAppIds() {
        RegistryStore registryStore = RegistryParser.parse(registryLines);
        RegistryObject object = registryStore.getObjectValue("Registry/HKCU/Software/Valve/Steam/apps");
        return object.getKeys().stream().map(Long::valueOf).sorted().collect(Collectors.toList());
    }
}
