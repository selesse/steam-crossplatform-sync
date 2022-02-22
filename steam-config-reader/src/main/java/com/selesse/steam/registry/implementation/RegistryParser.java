package com.selesse.steam.registry.implementation;

import com.google.common.collect.Iterables;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistryParser {
    private static final Pattern lineMatchingPattern = Pattern.compile("\t*\"(.+?)\"\t*\"(.*)\"");

    public static RegistryStore parse(List<String> lines) {
        return collapseRegistryStoreIfNecessary(parseWithoutRegistryCollapse(lines));
    }

    public static RegistryStore parseWithoutRegistryCollapse(List<String> lines) {
        if (Iterables.getLast(lines).isEmpty()) {
            lines = lines.subList(0, lines.size() - 1);
        }
        RegistryValue object = parseValue(lines);
        return new RegistryStore((RegistryObject) object);
    }

    private static RegistryStore collapseRegistryStoreIfNecessary(RegistryStore registryStore) {
        Set<String> keys = registryStore.getKeys();
        if (keys.size() == 1) {
            String keyValue = keys.stream().findFirst().orElse("");
            if (keyValue.matches("\\d+")) {
                return new RegistryStore(registryStore.getObjectValueAsObject(keyValue));
            }
        }
        return registryStore;
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
        Matcher matcher = lineMatchingPattern.matcher(line);
        boolean found = matcher.find();
        assert found;
        String key = matcher.group(1);
        String value = matcher.group(2);
        return new RegistryString(key, value);
    }
}
