package com.selesse.os;

class WindowsPathSanitizer {
    public static String sanitize(String input) {
        return input.replace("%USERPROFILE%", System.getenv("userprofile"))
                .replace("%LOCALAPPDATA%", System.getenv("localappdata"))
                .replace("%APPDATA%", System.getenv("appdata"));
    }
}
