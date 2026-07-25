package com.selesse.steam.registry;

import com.selesse.files.RuntimeExceptionFiles;
import com.selesse.os.FilePathSanitizer;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryParser;
import java.nio.file.Path;
import java.util.Optional;

public class SteamRegistry {
    public static SteamRegistry getInstance() {
        return new SteamRegistry();
    }

    public Path getAppCachePath() {
        return Path.of(getBasePath(), "appcache/appinfo.vdf");
    }

    public Path getSteamAppsPath() {
        return Path.of(getBasePath(), "steamapps");
    }

    public Optional<RegistryObject> readLibraryFolders() {
        return readVdfIfPresent(getSteamAppsPath().resolve("libraryfolders.vdf"));
    }

    public Optional<RegistryObject> readLoginUsers() {
        return readVdfIfPresent(Path.of(getBasePath(), "config/loginusers.vdf"));
    }

    public Optional<RegistryObject> readVdfIfPresent(Path path) {
        return path.toFile().isFile() ? Optional.of(readVdf(path)) : Optional.empty();
    }

    public RegistryObject readVdf(Path path) {
        return RegistryParser.parse(RuntimeExceptionFiles.readAllLines(path));
    }

    private String getBasePath() {
        return switch (OperatingSystems.get()) {
            case WINDOWS -> Path.of("C:\\Program Files (x86)\\Steam").toString();
            case MAC ->
                Path.of(FilePathSanitizer.sanitize("~/Library/Application Support/Steam"))
                        .toString();
            // ~/.steam has no steamapps symlink of its own; only ~/.steam/steam does.
            case LINUX, STEAM_OS ->
                Path.of(FilePathSanitizer.sanitize("~/.steam/steam")).toString();
        };
    }
}
