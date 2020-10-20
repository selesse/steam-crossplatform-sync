package com.selesse.os;

public class OperatingSystems {
    public enum OperatingSystem {
        WINDOWS, MAC
    }
    public static OperatingSystem get() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            return OperatingSystem.WINDOWS;
        } else if (osName.contains("darwin") || osName.contains("mac os")) {
            return OperatingSystem.MAC;
        }
        throw new IllegalArgumentException("Unsupported OS: " + osName);
    }
}
