package com.selesse.os;

import com.selesse.files.OsAgnosticPaths;

class WindowsPathSanitizer {
    public static String sanitize(String input) {
        return OsAgnosticPaths.of(input.replace("%USERPROFILE%", System.getenv("userprofile"))
                .replace("%LOCALAPPDATA%", System.getenv("localappdata"))
                .replace("%APPDATA%", System.getenv("appdata")));
    }
}
