package com.selesse.steam.registry;

import com.selesse.os.FilePathSanitizer;

import java.nio.file.Path;

public class LinuxSteamRegistry extends FileBasedRegistry {
    @Override
    Path registryPath() {
        return Path.of(FilePathSanitizer.sanitize("~/.steam/registry.vdf"));
    }
}
