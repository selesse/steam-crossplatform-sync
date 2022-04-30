package com.selesse.os;

public class FilePathSanitizer {
    public static String sanitize(String input) {
        return switch (OperatingSystems.get()) {
            case WINDOWS -> WindowsPathSanitizer.sanitize(input);
            case MAC, LINUX, STEAM_OS -> UnixPathSanitizer.sanitize(input);
        };
    }
}
