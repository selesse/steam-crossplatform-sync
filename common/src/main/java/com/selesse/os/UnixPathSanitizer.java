package com.selesse.os;

class UnixPathSanitizer {
    public static String sanitize(String input) {
        return input.replaceFirst("^~", System.getProperty("user.home"));
    }
}
