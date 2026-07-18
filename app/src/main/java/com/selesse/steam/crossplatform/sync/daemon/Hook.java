package com.selesse.steam.crossplatform.sync.daemon;

import com.google.common.annotations.VisibleForTesting;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class Hook {
    private static final Logger LOGGER = LoggerFactory.getLogger(Hook.class);
    // Dedicated so hook execution (e.g. session-end) can never be starved by unrelated work
    // queued on ForkJoinPool.commonPool() — such as a cloud-storage lookup stuck past its
    // intended timeout.
    @VisibleForTesting
    static final ExecutorService HOOK_EXECUTOR = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "hook-runner");
        thread.setDaemon(true);
        return thread;
    });

    abstract String name();

    abstract Map<String, String> env();

    void runAsync(SteamCrossplatformSyncConfig config) {
        CompletableFuture.runAsync(() -> run(config), HOOK_EXECUTOR).exceptionally(e -> {
            LOGGER.warn("Hook {} failed asynchronously", name(), e);
            return null;
        });
    }

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
        Path hooksDir = config.getConfigDirectory().resolve("hooks");

        Path hookPath = hooksDir.resolve(name());
        if (Files.isRegularFile(hookPath) && Files.isExecutable(hookPath)) {
            return hookPath;
        }

        Path batPath = hooksDir.resolve(name() + ".bat");
        if (Files.isRegularFile(batPath)) {
            return batPath;
        }

        LOGGER.warn("Hook {} not found in {}", name(), hooksDir.toAbsolutePath());
        return null;
    }
}
