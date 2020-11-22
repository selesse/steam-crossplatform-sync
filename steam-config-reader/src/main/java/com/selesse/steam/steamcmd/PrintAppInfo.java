package com.selesse.steam.steamcmd;

import com.google.common.collect.Maps;
import com.selesse.steam.registry.implementation.RegistryParser;
import com.selesse.steam.registry.implementation.RegistryStore;

import java.util.List;
import java.util.Map;

public class PrintAppInfo {
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
        Map<Long, List<String>> outputPerAppId = executor.runPrintAppInfoProcesses(appIds);
        for (Map.Entry<Long, List<String>> longListEntry : outputPerAppId.entrySet()) {
            RegistryStore registryStore = RegistryParser.parseOmittingFirstLevel(longListEntry.getValue());
            registryStoreMap.put(longListEntry.getKey(), registryStore);
        }
        return registryStoreMap;
    }
}
