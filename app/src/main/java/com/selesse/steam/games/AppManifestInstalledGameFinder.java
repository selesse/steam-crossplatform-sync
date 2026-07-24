package com.selesse.steam.games;

import com.selesse.files.RuntimeExceptionFiles;
import com.selesse.steam.registry.SteamRegistry;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryParser;
import com.selesse.steam.registry.implementation.RegistryStore;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AppManifestInstalledGameFinder implements InstalledGameFetcher {
    private static final String FULLY_INSTALLED_STATE_FLAGS = "4";

    @Override
    public List<Long> fetch() {
        return getLibrarySteamAppsPaths().stream()
                .flatMap(steamAppsPath -> findAppManifests(steamAppsPath).stream())
                .map(this::loadInstalledAppIdOrNull)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<Path> getLibrarySteamAppsPaths() {
        Path libraryFoldersPath = SteamRegistry.getInstance().getSteamAppsPath().resolve("libraryfolders.vdf");
        if (!libraryFoldersPath.toFile().isFile()) {
            return List.of();
        }

        RegistryStore registryStore = RegistryParser.parse(RuntimeExceptionFiles.readAllLines(libraryFoldersPath));
        RegistryObject libraries = registryStore.getObjectValueAsObject("libraryfolders");
        if (libraries == null) {
            return List.of();
        }

        return libraries.getKeys().stream()
                .map(key -> Path.of(libraries
                                .getObjectValueAsObject(key)
                                .getObjectValueAsString("path")
                                .getValue())
                        .resolve("steamapps"))
                .toList();
    }

    private List<File> findAppManifests(Path steamAppsPath) {
        File[] files = steamAppsPath.toFile().listFiles((dir, name) -> name.matches("appmanifest_\\d+\\.acf"));
        return files == null ? List.of() : Arrays.asList(files);
    }

    private Long loadInstalledAppIdOrNull(File appManifestFile) {
        RegistryStore registryStore =
                RegistryParser.parse(RuntimeExceptionFiles.readAllLines(appManifestFile.toPath()));
        RegistryObject appState = registryStore.getObjectValueAsObject("AppState");
        boolean fullyInstalled = appState != null
                && appState.pathExists("StateFlags")
                && appState.getObjectValueAsString("StateFlags").getValue().equals(FULLY_INSTALLED_STATE_FLAGS);
        if (!fullyInstalled) {
            return null;
        }
        return Long.valueOf(appState.getObjectValueAsString("appid").getValue());
    }
}
