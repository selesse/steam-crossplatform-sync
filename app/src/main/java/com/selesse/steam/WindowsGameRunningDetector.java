package com.selesse.steam;


import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WindowsGameRunningDetector {
    private static final Logger LOGGER = LoggerFactory.getLogger(WindowsGameRunningDetector.class);
    private static final String REGISTRY_COMMAND_TO_GET_APP_ID =
            "reg query HKEY_CURRENT_USER\\Software\\Valve\\Steam /v RunningAppId";

    public static boolean isGameCurrentlyRunning() {
        try {
            String registryOutput = runProcessAndGetOutput(REGISTRY_COMMAND_TO_GET_APP_ID);
            return appRunningBasedOnRegistryOutput(registryOutput);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean appRunningBasedOnRegistryOutput(String registryOutput) {
        Pattern regex = Pattern.compile("RunningAppId\\s+REG_DWORD\\s+0x(.*)");
        Matcher matcher = regex.matcher(registryOutput);
        if (matcher.find()) {
            String appHexId = matcher.group(1);
            long runningAppId = Long.parseLong(appHexId, 16);
            LOGGER.debug("Currently running Steam app ID is {}", runningAppId);
            return runningAppId > 0;
        }
        throw new RuntimeException("Could not parse output of registry");
    }

    private static String runProcessAndGetOutput(String command) throws IOException {
        List<String> commandParameters = Splitter.on(" ").splitToList(command);
        ProcessBuilder builder = new ProcessBuilder(commandParameters);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        InputStream is = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        return output.toString();
    }
}
