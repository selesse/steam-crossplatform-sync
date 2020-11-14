package com.selesse.os;

class MacPathSanitizer {
    public static String sanitize(String input) {
        return input.replaceFirst("^~", System.getProperty("user.home"));
    }
}
