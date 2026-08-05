package com.selesse.steam.registry;

import com.selesse.files.RuntimeExceptionFiles;
import com.selesse.os.FilePathSanitizer;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class SteamRegistry {
    public Path getAppCachePath() {
        return Path.of(getBasePath(), "appcache/appinfo.vdf");
    }

    public Path getSteamAppsPath() {
        return Path.of(getBasePath(), "steamapps");
    }

    public Optional<RegistryObject> readLibraryFolders() {
        return readVdfIfPresent(getSteamAppsPath().resolve("libraryfolders.vdf"));
    }

    /** Every library, not just the primary one - games routinely live on a second drive or SD card. */
    public List<Path> getLibrarySteamAppsPaths() {
        RegistryObject libraries = readLibraryFolders()
                .flatMap(registryObject -> registryObject.findObject("libraryfolders"))
                .orElse(null);
        if (libraries == null) {
            return List.of();
        }
        return libraries.getKeys().stream()
                .map(key -> libraries.getObjectValueAsObject(key).findString("path"))
                .flatMap(Optional::stream)
                .map(path -> Path.of(path.getValue()).resolve("steamapps"))
                .toList();
    }

    /**
     * The depot ids installed for {@code appId}, or empty when it isn't installed here. Against the
     * app cache's per-depot {@code oslist} this gives the build on disk, which {@code common/oslist}
     * cannot - that describes the store page rather than this machine.
     */
    public List<String> getInstalledDepotIds(long appId) {
        for (Path steamAppsPath : getLibrarySteamAppsPaths()) {
            Path manifest = steamAppsPath.resolve("appmanifest_" + appId + ".acf");
            Optional<RegistryObject> appState =
                    readVdfIfPresent(manifest).flatMap(registryObject -> registryObject.findObject("AppState"));
            if (appState.isPresent()) {
                return appState.get()
                        .findObject("InstalledDepots")
                        .map(RegistryObject::getKeys)
                        .orElseGet(List::of);
            }
        }
        return List.of();
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

    /** Steam keeps a prefix in whichever library it likes, not the one holding the game. */
    public Optional<Path> findProtonPrefix(long appId) {
        return getLibrarySteamAppsPaths().stream()
                .map(steamApps -> steamApps
                        .resolve("compatdata")
                        .resolve(String.valueOf(appId))
                        .resolve("pfx"))
                .filter(Files::isDirectory)
                .findFirst();
    }

    private String getBasePath() {
        return switch (OperatingSystems.get().family()) {
            case WINDOWS -> Path.of("C:\\Program Files (x86)\\Steam").toString();
            case MAC ->
                Path.of(FilePathSanitizer.sanitize("~/Library/Application Support/Steam"))
                        .toString();
            // ~/.steam has no steamapps symlink of its own; only ~/.steam/steam does.
            case LINUX -> Path.of(FilePathSanitizer.sanitize("~/.steam/steam")).toString();
        };
    }
}
