package com.selesse.os;

public class FilePathSanitizer {
    public static String sanitize(String input) {
        if (OperatingSystems.get() == OperatingSystems.OperatingSystem.WINDOWS) {
            return WindowsPathSanitizer.sanitize(input);
        } else if (OperatingSystems.get() == OperatingSystems.OperatingSystem.MAC) {
            return MacPathSanitizer.sanitize(input);
        }
        throw new IllegalArgumentException("Unsupported OS");
    }
}
