package com.selesse.steam.steamcmd;

import com.google.common.collect.Maps;
import com.selesse.steam.RegistryStores;
import com.selesse.steam.registry.implementation.RegistryParser;
import com.selesse.steam.registry.implementation.RegistryStore;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PrintAppInfo {
    private final Path cacheDirectory;

    public PrintAppInfo() {
        this.cacheDirectory = null;
    }

    public PrintAppInfo(Path cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }

    public RegistryStore getRegistryStore(Long appId) {
        return getRegistryStore(new PrintAppInfoExecutor(), appId);
    }

    public Map<Long, RegistryStore> getRegistryStores(List<Long> appIds) {
        return getRegistryStores(new PrintAppInfoExecutor(), appIds);
    }

    RegistryStore getRegistryStore(PrintAppInfoExecutor executor, Long appId) {
        List<String> lines = executor.runPrintAppInfoProcess(appId);
        return RegistryParser.parse(lines);
    }

    Map<Long, RegistryStore> getRegistryStores(PrintAppInfoExecutor executor, List<Long> appIds) {
        Map<Long, RegistryStore> registryStoreMap = Maps.newHashMap();

        if (cacheDirectory != null) {
            for (Long gameId : appIds) {
                Optional<List<String>> contents = RegistryStores.readCache(cacheDirectory, gameId);
                contents.ifPresent(
                        lines -> registryStoreMap.put(gameId, RegistryParser.parseOmittingFirstLevel(lines))
                );
            }
        }
        appIds.removeAll(registryStoreMap.keySet());

        if (!appIds.isEmpty()) {
            Map<Long, List<String>> outputPerAppId = executor.runPrintAppInfoProcesses(appIds);
            for (Map.Entry<Long, List<String>> longListEntry : outputPerAppId.entrySet()) {
                long gameId = longListEntry.getKey();
                List<String> gameRegistryLines = longListEntry.getValue();
                if (cacheDirectory != null) {
                    RegistryStores.cacheRegistryStore(cacheDirectory, gameId, gameRegistryLines);
                }
                RegistryStore registryStore = RegistryParser.parseOmittingFirstLevel(longListEntry.getValue());
                registryStoreMap.put(longListEntry.getKey(), registryStore);
            }
        }
        return registryStoreMap;
    }
}
