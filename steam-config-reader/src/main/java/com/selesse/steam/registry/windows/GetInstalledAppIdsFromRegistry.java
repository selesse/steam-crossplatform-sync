package com.selesse.steam.registry.windows;

import java.util.List;

public class GetInstalledAppIdsFromRegistry {
    public static List<Long> get() {
        List<Long> allApps = GetAllAppsFromRegistry.get();
        return allApps.parallelStream().filter(CheckGameInstallationFromRegistry::isInstalled).toList();
    }
}
