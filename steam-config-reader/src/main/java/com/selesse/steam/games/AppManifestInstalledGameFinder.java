package com.selesse.steam.games;

import com.selesse.steam.registry.SteamRegistry;
import com.selesse.steam.registry.implementation.RegistryObject;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AppManifestInstalledGameFinder implements InstalledGameFetcher {
    // StateFlags is a bitmask, not an enum - bit 4 (Fully Installed) can be combined with
    // other bits (e.g. 6 = Fully Installed | Update Required), so this can't be an equality check.
    private static final int FULLY_INSTALLED_BIT = 4;

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
        RegistryObject libraries = SteamRegistry.getInstance()
                .readLibraryFolders()
                .map(registryObject -> registryObject.getObjectValueAsObject("libraryfolders"))
                .orElse(null);
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
        RegistryObject registryObject = SteamRegistry.getInstance().readVdf(appManifestFile.toPath());
        RegistryObject appState = registryObject.getObjectValueAsObject("AppState");
        boolean fullyInstalled = appState != null
                && appState.pathExists("StateFlags")
                && isFullyInstalled(
                        appState.getObjectValueAsString("StateFlags").getValue());
        if (!fullyInstalled) {
            return null;
        }
        return Long.valueOf(appState.getObjectValueAsString("appid").getValue());
    }

    static boolean isFullyInstalled(String stateFlags) {
        return (Integer.parseInt(stateFlags) & FULLY_INSTALLED_BIT) != 0;
    }
}
