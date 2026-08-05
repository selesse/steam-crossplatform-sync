package com.selesse.steam.registry.implementation;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistryParser {
    private static final Pattern lineMatchingPattern = Pattern.compile("\t*\"(.+?)\"\t*\"(.*)\"", Pattern.DOTALL);
    private static final Pattern multiLinePatternStart = Pattern.compile("\t*\"(.+?)\"\t*\"[^\"]*");

    public static RegistryObject parse(List<String> lines) {
        if (Iterables.getLast(lines).isEmpty()) {
            lines = lines.subList(0, lines.size() - 1);
        }
        return (RegistryObject) parseValue(lines);
    }

    /**
     * Parses one app's entry as the app cache writes it, wrapped in a block named after its appid:
     *
     * <pre>{@code
     * "646570"
     * {
     *   "common" { ... }
     * }
     * }</pre>
     *
     * <p>The wrapper is dropped, so the result is the app's own body and paths read as
     * {@code common/name} rather than {@code 646570/common/name}. An entry that isn't shaped that
     * way is returned as parsed.
     */
    public static RegistryObject parseAppCacheEntry(List<String> lines) {
        RegistryObject registryObject = parse(lines);
        List<String> keys = registryObject.getKeys();
        if (keys.size() == 1 && keys.get(0).matches("\\d+")) {
            return registryObject.getObjectValueAsObject(keys.get(0));
        }
        return registryObject;
    }

    private static RegistryValue parseValue(List<String> blockScope) {
        RegistryObject registryObject = new RegistryObject();
        for (int currentLineNumber = 0; currentLineNumber < blockScope.size(); ) {
            String line = blockScope.get(currentLineNumber);
            if (isString(line)) {
                RegistryString string = parseRegistryString(line);
                registryObject.put(string.getName(), string);
                currentLineNumber++;
            } else if (isMultilineString(line)) {
                int stringLength = getMultilineStringLength(currentLineNumber, blockScope);
                String collapsedString =
                        String.join("\n", blockScope.subList(currentLineNumber, currentLineNumber + stringLength));
                RegistryString string = parseRegistryString(collapsedString);
                registryObject.put(string.getName(), string);
                currentLineNumber = currentLineNumber + stringLength;
            } else {
                if (currentLineNumber + 1 < blockScope.size()) {
                    String nextLine = blockScope.get(currentLineNumber + 1);
                    int endBlock = -1;
                    for (int i = currentLineNumber + 1; i < blockScope.size(); i++) {
                        if (blockScope.get(i).equals(nextLine.replace("{", "}"))) {
                            endBlock = i;
                            break;
                        }
                    }
                    List<String> subBlock = blockScope.subList(currentLineNumber + 2, endBlock);
                    registryObject.put(extractKeyName(line), parseValue(subBlock));
                    currentLineNumber = endBlock + 1;
                }
            }
        }
        return registryObject;
    }

    private static boolean isMultilineString(String line) {
        return multiLinePatternStart.matcher(line).matches();
    }

    private static int getMultilineStringLength(int currentLineNumber, List<String> blockScope) {
        int stopIndex = -1;
        for (int i = currentLineNumber + 1; i < blockScope.size(); i++) {
            if (blockScope.get(i).matches("(.*)?\"")) {
                stopIndex = i;
                break;
            }
        }
        return stopIndex - currentLineNumber + 1;
    }

    private static String extractKeyName(String line) {
        String trim = line.trim();
        return trim.substring(1, trim.length() - 1);
    }

    private static boolean isString(String line) {
        return lineMatchingPattern.matcher(line).find();
    }

    private static RegistryString parseRegistryString(String line) {
        Matcher matcher = lineMatchingPattern.matcher(line);
        boolean found = matcher.find();
        assert found;
        String key = matcher.group(1);
        String value = matcher.group(2);
        return new RegistryString(key, value);
    }
}
