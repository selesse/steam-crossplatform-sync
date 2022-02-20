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

    public List<String> asVdfString(Long appId) {
        Optional<List<String>> appLines = Optional.empty();
        if (cacheDirectory != null) {
            appLines = RegistryStores.readCache(cacheDirectory, appId);
        }
        return appLines.orElseGet(() -> {
            List<String> lines = new PrintAppInfoExecutor().runPrintAppInfoProcess(appId);
            cacheDirectoryMaybe().ifPresent(x -> RegistryStores.cacheRegistryStore(x, appId, lines));
            return lines;
        });
    }

    RegistryStore getRegistryStore(PrintAppInfoExecutor executor, Long appId) {
        Optional<RegistryStore> registry = Optional.empty();
        if (cacheDirectory != null) {
            registry = RegistryStores.readCache(cacheDirectory, appId).map(RegistryParser::parseOmittingFirstLevel);
        }
        return registry.orElseGet(() -> {
            List<String> lines = executor.runPrintAppInfoProcess(appId);
            cacheDirectoryMaybe().ifPresent(x -> RegistryStores.cacheRegistryStore(x, appId, lines));
            return RegistryParser.parseOmittingFirstLevel(lines);
        });
    }

    Map<Long, RegistryStore> getRegistryStores(PrintAppInfoExecutor executor, List<Long> appIds) {
        Map<Long, RegistryStore> registryStoreMap = Maps.newHashMap();

        cacheDirectoryMaybe().ifPresent(cacheDir -> {
            for (Long gameId : appIds) {
                Optional<List<String>> contents = RegistryStores.readCache(cacheDir, gameId);
                contents.ifPresent(
                        lines -> registryStoreMap.put(gameId, RegistryParser.parseOmittingFirstLevel(lines))
                );
            }
        });
        appIds.removeAll(registryStoreMap.keySet());

        if (!appIds.isEmpty()) {
            Map<Long, List<String>> outputPerAppId = executor.runPrintAppInfoProcesses(appIds);
            for (Map.Entry<Long, List<String>> longListEntry : outputPerAppId.entrySet()) {
                long gameId = longListEntry.getKey();
                List<String> gameRegistryLines = longListEntry.getValue();
                cacheDirectoryMaybe().ifPresent(x -> RegistryStores.cacheRegistryStore(x, gameId, gameRegistryLines));
                RegistryStore registryStore = RegistryParser.parseOmittingFirstLevel(longListEntry.getValue());
                registryStoreMap.put(longListEntry.getKey(), registryStore);
            }
        }
        return registryStoreMap;
    }

    private Optional<Path> cacheDirectoryMaybe() {
        return Optional.ofNullable(cacheDirectory);
    }
}
