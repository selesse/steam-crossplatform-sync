package com.selesse.os;

public class FilePathSanitizer {
    public static String sanitize(String input) {
        return switch (OperatingSystems.get()) {
            case WINDOWS -> WindowsPathSanitizer.sanitize(input);
            case MAC, LINUX -> UnixPathSanitizer.sanitize(input);
        };
    }
}
