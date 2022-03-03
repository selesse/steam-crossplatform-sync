package com.selesse.files;

import java.util.regex.Pattern;

public class OsAgnosticPaths {
    public static String of(String path) {
        return path.replaceAll(Pattern.quote("\\"), "/");
    }
}
