package com.selesse.steam.registry;

import com.selesse.os.FilePathSanitizer;
import com.selesse.steam.registry.mac.MacSteamRegistryFile;
import com.selesse.steam.steamcmd.games.SteamGameMetadata;

import java.nio.file.Path;
import java.util.List;

public class LinuxSteamRegistry extends SteamRegistry {
    private static final String REGISTRY_PATH = "~/.steam/registry.vdf";
    private final Path registryFilePath;
    private final MacSteamRegistryFile registryFile;

    public LinuxSteamRegistry() {
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

    @Override
    public SteamGameMetadata getGameMetadata(Long gameId) {
        return registryFile.getGameMetadata(gameId);
    }

    private void ensureRegistryFileExists() {
        if (!registryFilePath.toFile().isFile()) {
            throw new RuntimeException("Registry file does not exist in " + registryFilePath.toAbsolutePath());
        }
    }
}
