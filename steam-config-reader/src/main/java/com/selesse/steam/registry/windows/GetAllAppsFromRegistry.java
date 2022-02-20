package com.selesse.steam.registry.windows;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.selesse.processes.ProcessRunner;

import java.util.List;
import java.util.stream.Collectors;

public class GetAllAppsFromRegistry {
    private static final List<String> ALL_APPS_FROM_REGISTRY = List.of(
            "reg", "query", "HKEY_CURRENT_USER\\Software\\Valve\\Steam\\apps"
    );

    public static List<Long> get() {
        String registryOutput = new ProcessRunner(ALL_APPS_FROM_REGISTRY).runAndGetOutput();
        List<String> perLineOutput = Splitter.on("\n").omitEmptyStrings().splitToList(registryOutput);
        return perLineOutput.stream().map(
                // e.g. HKEY_CURRENT_USER\Software\Valve\Steam\apps\1005300 => 1005300
                line -> Long.parseLong(Iterables.getLast(Splitter.on("\\").splitToList(line)))
        ).collect(Collectors.toList());
    }
}
