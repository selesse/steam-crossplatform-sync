package com.selesse.steam;

import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryParser;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds a VDF fixture from a readable text block.
 *
 * <p>{@link RegistryParser} needs what Valve actually writes: tab indentation, and tabs between a
 * key and its value. Spelling that out in Java turns a fixture into a wall of
 * {@code "\t\t\"key\"\t\"value\""} where the structure is impossible to see. Write the block with
 * two-space indentation and a single space between key and value, and this converts it:
 *
 * <pre>{@code
 * "common"
 * {
 *   "name" "Test Game"
 * }
 * }</pre>
 *
 * <p>Braces stay on their own lines, as they are in the real format - the parser finds the end of a
 * block by matching the opening brace line with its {@code {} swapped for {@code }}, so the two have
 * to be indented alike.
 */
public final class TestVdf {
    private static final Pattern KEY_AND_VALUE = Pattern.compile("^(\"[^\"]*\") +(\"[^\"]*\")$");
    private static final int SPACES_PER_INDENT = 2;

    private TestVdf() {}

    public static RegistryObject parse(String vdf) {
        return RegistryParser.parse(lines(vdf));
    }

    public static List<String> lines(String vdf) {
        return vdf.strip().lines().map(TestVdf::toVdfLine).toList();
    }

    private static String toVdfLine(String line) {
        int indent = (line.length() - line.stripLeading().length()) / SPACES_PER_INDENT;
        String body = line.strip();
        Matcher keyAndValue = KEY_AND_VALUE.matcher(body);
        if (keyAndValue.matches()) {
            body = keyAndValue.group(1) + "\t" + keyAndValue.group(2);
        }
        return "\t".repeat(indent) + body;
    }
}
