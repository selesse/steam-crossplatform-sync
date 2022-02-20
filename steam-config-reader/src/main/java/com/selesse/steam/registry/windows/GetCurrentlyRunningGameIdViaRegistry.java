package com.selesse.steam.registry.windows;

import com.google.common.base.Splitter;
import com.selesse.processes.ProcessRunner;

public class GetCurrentlyRunningGameIdViaRegistry {
    private static final String REGISTRY_COMMAND_TO_GET_APP_ID =
            "reg query HKEY_CURRENT_USER\\Software\\Valve\\Steam /v RunningAppId";

    public static long get() {
        String registryOutput =
                new ProcessRunner(Splitter.on(" ").splitToList(REGISTRY_COMMAND_TO_GET_APP_ID)).runAndGetOutput();
        return RegistryDwordParser.getValueFromOutput(registryOutput, "RunningAppId").orElseThrow();
    }
}
