package com.selesse.steam;

import com.google.common.base.Splitter;
import com.selesse.os.FilePathSanitizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class MacSteamRegistry extends SteamRegistry {
    private static final String REGISTRY_PATH = "~/Library/Application Support/Steam/registry.vdf";
    private final Path registryFilePath;

    public MacSteamRegistry() {
        registryFilePath = Path.of(FilePathSanitizer.sanitize(REGISTRY_PATH));
    }

    @Override
    public long getCurrentlyRunningAppId() {
        ensureRegistryFileExists();
        try {
            List<String> lines = Files.readAllLines(registryFilePath);
            return lines.stream()
                    .filter(line -> line.contains("RunningAppID"))
                    .map(line -> {
                        List<String> partitions = Splitter.on("\"").splitToList(line);
                        return Long.valueOf(partitions.get(partitions.size() - 2));
                    })
                    .findFirst()
                    .orElseThrow();
        } catch (IOException e) {
            throw new RuntimeException("Unable to get app ID from file");
        }
    }

    private void ensureRegistryFileExists() {
        if (!registryFilePath.toFile().isFile()) {
            throw new RuntimeException("Registry file does not exist in " + registryFilePath.toAbsolutePath());
        }
    }

}
