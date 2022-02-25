package com.selesse.os;

public class OperatingSystems {
    public enum OperatingSystem {
        WINDOWS,
        MAC,
        LINUX
    }

    public static OperatingSystem get() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            return OperatingSystem.WINDOWS;
        } else if (osName.contains("darwin") || osName.contains("mac os")) {
            return OperatingSystem.MAC;
        } else if (osName.contains("linux")) {
            return OperatingSystem.LINUX;
        }
        throw new IllegalArgumentException("Unsupported OS: " + osName);
    }
}
