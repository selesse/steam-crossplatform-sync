package com.selesse.steam.crossplatform.sync.daemon;

import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class Hook {
    private static final Logger LOGGER = LoggerFactory.getLogger(Hook.class);

    abstract String name();

    abstract Map<String, String> env();

    void run(SteamCrossplatformSyncConfig config) {
        Path hookPath = resolveHookPath(config);
        if (hookPath == null) {
            return;
        }
        try {
            ProcessBuilder pb = hookPath.toString().endsWith(".bat")
                    ? new ProcessBuilder("cmd", "/c", hookPath.toAbsolutePath().toString())
                    : new ProcessBuilder(hookPath.toAbsolutePath().toString());
            pb.environment().putAll(env());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                LOGGER.warn("Hook {} exited {}{}", name(), exitCode, output.isBlank() ? "" : " " + output.strip());
            } else {
                LOGGER.info("Hook {} exited {}", name(), exitCode);
                LOGGER.debug("Hook {} output: {}", name(), output.strip());
            }
        } catch (Exception e) {
            LOGGER.warn("Hook {} failed", name(), e);
        }
    }

    private Path resolveHookPath(SteamCrossplatformSyncConfig config) {
        Path hookPath = config.getConfigDirectory().resolve("hooks").resolve(name());
        if (Files.isRegularFile(hookPath) && Files.isExecutable(hookPath)) {
            return hookPath;
        }
        Path batPath = config.getConfigDirectory().resolve("hooks").resolve(name() + ".bat");
        if (Files.isRegularFile(batPath)) {
            return batPath;
        }
        return null;
    }
}
