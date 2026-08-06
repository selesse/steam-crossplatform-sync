package com.selesse.text;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Splits a string on a literal separator, keeping empty segments (including a trailing one) -
 * unlike {@link String#split(String)}, which drops trailing empty strings by default.
 */
public final class Splitter {
    private final Pattern pattern;

    private Splitter(String literalSeparator) {
        this.pattern = Pattern.compile(Pattern.quote(literalSeparator));
    }

    public static Splitter on(String separator) {
        return new Splitter(separator);
    }

    public static Splitter on(char separator) {
        return new Splitter(String.valueOf(separator));
    }

    public List<String> splitToList(String input) {
        return List.of(pattern.split(input, -1));
    }
}
