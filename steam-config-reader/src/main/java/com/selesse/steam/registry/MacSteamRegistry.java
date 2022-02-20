package com.selesse.steam.registry;

import com.selesse.os.FilePathSanitizer;

import java.nio.file.Path;

class MacSteamRegistry extends FileBasedRegistry {
    @Override
    Path registryPath() {
        return Path.of(FilePathSanitizer.sanitize("~/Library/Application Support/Steam/registry.vdf"));
    }
}
