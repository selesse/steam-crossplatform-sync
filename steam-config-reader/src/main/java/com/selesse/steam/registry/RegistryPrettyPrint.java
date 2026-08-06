package com.selesse.steam.registry;

import com.selesse.collections.MapCollectors;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryString;
import com.selesse.steam.registry.implementation.RegistryValue;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * Pretty prints a {@link RegistryObject}, using the same format as steamcmd.
 */
public class RegistryPrettyPrint {
    public static String prettyPrint(RegistryObject registryObject) {
        Map<String, RegistryValue> keyValuePairs =
                registryObject.getKeys().stream().collect(keyAndValueCollector(registryObject));
        return prettyPrintKeyValuePairs(keyValuePairs);
    }

    private static String prettyPrintKeyValuePairs(Map<String, RegistryValue> keyValuePairs) {
        int indentLevel = 0;
        StringBuilder stringBuilder = new StringBuilder();
        boolean hasAppId = keyValuePairs.containsKey("appid");

        if (hasAppId) {
            RegistryString appId = (RegistryString) keyValuePairs.get("appid");
            keyValuePairs.remove("appid");

            stringBuilder.append("\"").append(appId.getValue()).append("\"").append("\n");
            stringBuilder.append("{\n");
            indentLevel = 1;
        }
        String indent = "\t".repeat(indentLevel);
        for (Map.Entry<String, RegistryValue> x : keyValuePairs.entrySet()) {
            stringBuilder
                    .append(indent)
                    .append("\"")
                    .append(x.getKey())
                    .append("\"")
                    .append("\n");
            stringBuilder.append(indent).append("{\n");
            stringBuilder.append(prettyPrint(indentLevel + 1, x.getValue()));
            stringBuilder.append(indent).append("}\n");
        }
        if (hasAppId) {
            stringBuilder.append("}\n");
        }
        return stringBuilder.toString();
    }

    private static String prettyPrint(int indentLevel, RegistryValue value) {
        StringBuilder stringBuilder = new StringBuilder();
        String indent = "\t".repeat(indentLevel);
        switch (value) {
            case RegistryString string -> stringBuilder.append(indent).append(printRegistry(string));
            case RegistryObject registryObject -> {
                Map<String, RegistryValue> keyValuePairs =
                        registryObject.getKeys().stream().collect(keyAndValueCollector(registryObject));
                for (Map.Entry<String, RegistryValue> x : keyValuePairs.entrySet()) {
                    if (x.getValue() instanceof RegistryString string) {
                        stringBuilder.append(indent).append(printRegistry(string));
                    } else {
                        stringBuilder
                                .append(indent)
                                .append("\"")
                                .append(x.getKey())
                                .append("\"")
                                .append("\n");
                        stringBuilder.append(indent).append("{\n");
                        stringBuilder.append(prettyPrint(indentLevel + 1, x.getValue()));
                        stringBuilder.append(indent).append("}\n");
                    }
                }
            }
        }

        return stringBuilder.toString();
    }

    private static String printRegistry(RegistryString registryString) {
        return "%s%s%s\n".formatted(quote(registryString.getName()), "\t".repeat(2), quote(registryString.getValue()));
    }

    private static String quote(String value) {
        return "\"" + value + "\"";
    }

    private static Collector<String, ?, Map<String, RegistryValue>> keyAndValueCollector(RegistryObject value) {
        return MapCollectors.toLinkedMap(Function.identity(), value::get);
    }
}
