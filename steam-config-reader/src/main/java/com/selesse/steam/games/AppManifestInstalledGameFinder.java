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

    private final SteamRegistry registry;

    public AppManifestInstalledGameFinder(SteamRegistry registry) {
        this.registry = registry;
    }

    @Override
    public List<Long> fetch() {
        return registry.getLibrarySteamAppsPaths().stream()
                .flatMap(steamAppsPath -> findAppManifests(steamAppsPath).stream())
                .map(this::loadInstalledAppIdOrNull)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<File> findAppManifests(Path steamAppsPath) {
        File[] files = steamAppsPath.toFile().listFiles((dir, name) -> name.matches("appmanifest_\\d+\\.acf"));
        return files == null ? List.of() : Arrays.asList(files);
    }

    private Long loadInstalledAppIdOrNull(File appManifestFile) {
        RegistryObject registryObject = registry.readVdf(appManifestFile.toPath());
        RegistryObject appState = registryObject.findObject("AppState").orElse(null);
        if (appState == null) {
            return null;
        }
        boolean fullyInstalled = appState.findString("StateFlags")
                .map(stateFlags -> isFullyInstalled(stateFlags.getValue()))
                .orElse(false);
        if (!fullyInstalled) {
            return null;
        }
        return Long.valueOf(appState.getObjectValueAsString("appid").getValue());
    }

    static boolean isFullyInstalled(String stateFlags) {
        return (Integer.parseInt(stateFlags) & FULLY_INSTALLED_BIT) != 0;
    }
}
