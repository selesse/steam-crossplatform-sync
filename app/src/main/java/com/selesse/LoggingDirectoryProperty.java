package com.selesse;

import ch.qos.logback.core.PropertyDefinerBase;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import java.io.File;
import java.util.Objects;

public class LoggingDirectoryProperty extends PropertyDefinerBase {
    @Override
    public String getPropertyValue() {
        if (runningInIde()) {
            return System.getProperty("user.dir") + File.separatorChar + "log";
        } else {
            // Not load(): that logs, and we're still inside logback's own bootstrap here.
            return SteamCrossplatformSyncConfig.defaultConfigDirectory()
                    .toAbsolutePath()
                    .toString();
        }
    }

    private boolean runningInIde() {
        String javaClassPath = System.getProperty("java.class.path");
        return Objects.requireNonNull(javaClassPath).contains("build" + File.separatorChar + "classes");
    }
}
