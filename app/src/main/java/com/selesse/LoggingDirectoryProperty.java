package com.selesse;

import ch.qos.logback.core.PropertyDefinerBase;
import com.google.common.base.StandardSystemProperty;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSync;
import com.selesse.steam.crossplatform.sync.config.SteamCrossplatformSyncConfig;
import java.io.File;
import java.util.Objects;

public class LoggingDirectoryProperty extends PropertyDefinerBase {
    @Override
    public String getPropertyValue() {
        if (runningInIde()) {
            return StandardSystemProperty.USER_DIR.value() + File.separatorChar + "log";
        } else {
            SteamCrossplatformSyncConfig config = SteamCrossplatformSync.loadConfiguration();
            return config.getConfigDirectory().toAbsolutePath().toString();
        }
    }

    private boolean runningInIde() {
        String javaClassPath = StandardSystemProperty.JAVA_CLASS_PATH.value();
        return Objects.requireNonNull(javaClassPath).contains("build" + File.separatorChar + "classes");
    }
}
