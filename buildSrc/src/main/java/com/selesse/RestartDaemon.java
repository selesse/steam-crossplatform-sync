package com.selesse;

import org.gradle.api.Project;
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform;
import org.gradle.nativeplatform.platform.internal.DefaultOperatingSystem;

import java.io.File;
import java.io.IOException;

public class RestartDaemon {
    private final File shadowJarPath;
    private final Project project;

    public RestartDaemon(Project project, File shadowJarPath) {
        this.project = project;
        this.shadowJarPath = shadowJarPath;
    }

    public void restart() throws IOException, InterruptedException {
        DefaultOperatingSystem currentOperatingSystem = DefaultNativePlatform.getCurrentOperatingSystem();
        if (currentOperatingSystem.isMacOsX()) {
            new RestartMacDaemon().restart();
        } else if (currentOperatingSystem.isWindows()) {
            new RestartWindowsDaemon(project, shadowJarPath).restart();
        }
    }
}
