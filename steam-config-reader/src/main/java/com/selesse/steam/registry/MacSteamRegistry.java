package com.selesse.steam.registry;

import com.selesse.os.FilePathSanitizer;
import com.selesse.steam.registry.mac.MacSteamRegistryFile;
import com.selesse.steam.steamcmd.games.SteamGameMetadata;

import java.nio.file.Path;
import java.util.List;

class MacSteamRegistry extends SteamRegistry {
    private static final String REGISTRY_PATH = "~/Library/Application Support/Steam/registry.vdf";
    private final Path registryFilePath;
    private final MacSteamRegistryFile registryFile;

    public MacSteamRegistry() {
        registryFilePath = Path.of(FilePathSanitizer.sanitize(REGISTRY_PATH));
        registryFile = new MacSteamRegistryFile(registryFilePath);
        ensureRegistryFileExists();
    }

    @Override
    public long getCurrentlyRunningAppId() {
        return registryFile.getCurrentlyRunningAppId();
    }

    @Override
    public List<Long> getInstalledAppIds() {
        return registryFile.getInstalledAppIds();
    }

    @Override
    public List<SteamGameMetadata> getGameMetadata() {
        return registryFile.getGameMetadata();
    }

    private void ensureRegistryFileExists() {
        if (!registryFilePath.toFile().isFile()) {
            throw new RuntimeException("Registry file does not exist in " + registryFilePath.toAbsolutePath());
        }
    }
}
