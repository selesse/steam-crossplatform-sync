package com.selesse.os;

import java.nio.file.Files;
import java.nio.file.Path;

public class OperatingSystems {
    public enum OperatingSystem {
        WINDOWS,
        MAC,
        LINUX,
        STEAM_OS
    }

    public static OperatingSystem get() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            return OperatingSystem.WINDOWS;
        } else if (osName.contains("darwin") || osName.contains("mac os")) {
            return OperatingSystem.MAC;
        } else if (osName.contains("linux")) {
            if (Files.exists(Path.of("/etc/steamos-release"))) {
                return OperatingSystem.STEAM_OS;
            }
            return OperatingSystem.LINUX;
        }
        throw new IllegalArgumentException("Unsupported OS: " + osName);
    }
}
