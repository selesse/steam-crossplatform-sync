package com.selesse.steam.registry.windows;

import com.google.common.base.Splitter;
import com.selesse.processes.ProcessRunner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetCurrentlyRunningGameIdViaRegistry {
    private static final String REGISTRY_COMMAND_TO_GET_APP_ID =
            "reg query HKEY_CURRENT_USER\\Software\\Valve\\Steam /v RunningAppId";

    public static long get() {
        String registryOutput =
                new ProcessRunner(Splitter.on(" ").splitToList(REGISTRY_COMMAND_TO_GET_APP_ID)).runAndGetOutput();
        return currentlyRunningAppIdBasedOnRegistryOutput(registryOutput);
    }

    private static long currentlyRunningAppIdBasedOnRegistryOutput(String registryOutput) {
        Pattern regex = Pattern.compile("RunningAppId\\s+REG_DWORD\\s+0x(.*)");
        Matcher matcher = regex.matcher(registryOutput);
        if (matcher.find()) {
            String appHexId = matcher.group(1);
            return Long.parseLong(appHexId, 16);
        }
        throw new RuntimeException("Could not parse output of registry");
    }
}
