package com.selesse.steam.steamcmd;

import com.selesse.steam.registry.implementation.RegistryParser;
import com.selesse.steam.registry.implementation.RegistryStore;

import java.util.List;

public class PrintAppInfo {
    public RegistryStore getRegistryStore(Long appId) {
        return getRegistryStore(new PrintAppInfoExecutor(), appId);
    }

    RegistryStore getRegistryStore(PrintAppInfoExecutor executor, Long appId) {
        List<String> lines = executor.runPrintAppInfoProcess(appId);
        return RegistryParser.parse(lines);
    }
}
