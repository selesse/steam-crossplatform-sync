package com.selesse.steam.mac;

import com.google.common.base.Splitter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
}
