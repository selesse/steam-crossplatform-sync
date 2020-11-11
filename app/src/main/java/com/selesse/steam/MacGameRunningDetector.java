package com.selesse.steam;

import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.selesse.os.FilePathSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

class MacGameRunningDetector {
    private static final Logger LOGGER = LoggerFactory.getLogger(MacGameRunningDetector.class);
    private static final String REGISTRY_PATH = "~/Library/Application Support/Steam/registry.vdf";

    public static boolean isGameCurrentlyRunning() {
        File steamRegistryFile = new File(FilePathSanitizer.sanitize(REGISTRY_PATH));
        try {
            List<String> lines = Files.readLines(steamRegistryFile, StandardCharsets.UTF_8);
            int runningAppId = lines.stream()
                    .filter(line -> line.contains("RunningAppID"))
                    .map(line -> {
                        List<String> partitions = Splitter.on("\"").splitToList(line);
                        return Integer.valueOf(partitions.get(partitions.size() - 2));
                    })
                    .findFirst()
                    .orElseThrow();
            if (runningAppId > 0) {
                LOGGER.info("Currently running app: {}", runningAppId);
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
