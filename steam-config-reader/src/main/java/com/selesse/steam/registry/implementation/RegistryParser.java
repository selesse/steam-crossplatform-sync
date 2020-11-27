package com.selesse.steam.registry.implementation;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.regex.Pattern;

public class RegistryParser {
    private static final Pattern lineMatchingPattern = Pattern.compile("\t*\".+?\"\t*\".*\"");

    public static RegistryStore parse(List<String> lines) {
        RegistryValue object = parseValue(lines);
        return new RegistryStore((RegistryObject) object);
    }

    public static RegistryStore parseOmittingFirstLevel(List<String> lines) {
        RegistryValue object = parseValue(lines);
        RegistryObject firstLevelKey = (RegistryObject) object;
        return new RegistryStore((RegistryObject) firstLevelKey.get(firstLevelKey.getKeys().get(0)));
    }

    private static RegistryValue parseValue(List<String> blockScope) {
        RegistryObject registryObject = new RegistryObject();
        for (int currentLineNumber = 0; currentLineNumber < blockScope.size();) {
            String line = blockScope.get(currentLineNumber);
            if (isString(line)) {
                RegistryString string = parseRegistryString(line);
                registryObject.put(string.getName(), string);
                currentLineNumber++;
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

    private static String extractKeyName(String line) {
        String trim = line.trim();
        return trim.substring(1, trim.length() - 1);
    }

    private static boolean isString(String line) {
        return lineMatchingPattern.matcher(line).find();
    }

    private static RegistryString parseRegistryString(String line) {
        List<String> strings = Splitter.on("\t").omitEmptyStrings().splitToList(line);
        String key = extractKeyName(strings.get(0));
        String value = extractKeyName(strings.get(1));
        return new RegistryString(key, value);
    }
}
