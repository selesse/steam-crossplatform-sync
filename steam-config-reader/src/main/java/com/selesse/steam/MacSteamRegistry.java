package com.selesse.steam;

import com.selesse.os.FilePathSanitizer;
import com.selesse.steam.mac.MacSteamRegistryFile;

import java.nio.file.Path;

class MacSteamRegistry extends SteamRegistry {
    private static final String REGISTRY_PATH = "~/Library/Application Support/Steam/registry.vdf";
    private final Path registryFilePath;
    private final MacSteamRegistryFile registryFile;

    public MacSteamRegistry() {
        registryFilePath = Path.of(FilePathSanitizer.sanitize(REGISTRY_PATH));
        registryFile = new MacSteamRegistryFile(registryFilePath);
    }

    @Override
    public long getCurrentlyRunningAppId() {
        ensureRegistryFileExists();
        return registryFile.getCurrentlyRunningAppId();
    }

    private void ensureRegistryFileExists() {
        if (!registryFilePath.toFile().isFile()) {
            throw new RuntimeException("Registry file does not exist in " + registryFilePath.toAbsolutePath());
        }
    }

}
