package com.selesse.files;

public class OsAgnosticPaths {
    public static String of(String path) {
        return path.replace("\\", "/");
    }
}
