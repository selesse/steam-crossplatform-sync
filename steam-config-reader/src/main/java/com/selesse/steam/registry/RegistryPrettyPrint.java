package com.selesse.steam.registry;

import com.google.common.base.Strings;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryStore;
import com.selesse.steam.registry.implementation.RegistryString;
import com.selesse.steam.registry.implementation.RegistryValue;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class RegistryPrettyPrint {
    private final RegistryStore gameRegistryStore;
    private final List<String> STEAM_MAIN_SECTION_ORDERING = List.of("common", "extended", "config", "depots", "ufs");

    public RegistryPrettyPrint(RegistryStore gameRegistryStore) {
        this.gameRegistryStore = gameRegistryStore;
    }

    public String prettyPrint() {
        int indentLevel = 0;

        StringBuilder stringBuilder = new StringBuilder();
        Map<String, RegistryValue> keyValuePairs =
                gameRegistryStore.getKeys().stream().collect(keyAndValueCollector(gameRegistryStore));

        boolean hasAppId = keyValuePairs.containsKey("appid");

        if (hasAppId) {
            RegistryString appId = (RegistryString) keyValuePairs.get("appid");
            keyValuePairs.remove("appid");

            stringBuilder.append("\"").append(appId.getValue()).append("\"").append("\n");
            stringBuilder.append("{\n");
            indentLevel = 1;
        }
        String indent = Strings.repeat("\t", indentLevel);
        for (Map.Entry<String, RegistryValue> x : orderEntriesBasedOnSteam(keyValuePairs)) {
            stringBuilder.append(indent).append("\"").append(x.getKey()).append("\"").append("\n");
            stringBuilder.append(indent).append("{\n");
            stringBuilder.append(prettyPrint(indentLevel + 1, x.getValue()));
            stringBuilder.append(indent).append("}\n");
        }
        if (hasAppId) {
            stringBuilder.append("}\n");
        }
        return stringBuilder.toString();
    }

    private String prettyPrint(int indentLevel, RegistryValue value) {
        StringBuilder stringBuilder = new StringBuilder();
        String indent = Strings.repeat("\t", indentLevel);
        if (value instanceof RegistryObject) {
            Map<String, RegistryValue> keyValuePairs =
                    ((RegistryObject) value).getKeys().stream().collect(keyAndValueCollector((RegistryObject) value));
            for (Map.Entry<String, RegistryValue> x : orderEntriesBasedOnSteam(keyValuePairs)) {
                if (x.getValue() instanceof RegistryString) {
                    stringBuilder.append(indent).append(printRegistry((RegistryString) x.getValue()));
                } else {
                    stringBuilder.append(indent).append("\"").append(x.getKey()).append("\"").append("\n");
                    stringBuilder.append(indent).append("{\n");
                    stringBuilder.append(prettyPrint(indentLevel + 1, x.getValue()));
                    stringBuilder.append(indent).append("}\n");
                }
            }
        } else if (value instanceof RegistryString) {
            stringBuilder.append(indent).append(printRegistry((RegistryString) value));
        }

        return stringBuilder.toString();
    }

    private String printRegistry(RegistryString registryString) {
        return "%s%s%s\n".formatted(quote(registryString.getName()), Strings.repeat("\t", 2), quote(registryString.getValue()));
    }

    private String quote(String value) {
        return "\"" + value + "\"";
    }

    private Collector<String, ?, Map<String, RegistryValue>> keyAndValueCollector(RegistryObject value) {
        return Collectors.toMap(Function.identity(), value::getObjectValue);
    }

    private Collector<String, ?, Map<String, RegistryValue>> keyAndValueCollector(RegistryStore value) {
        return Collectors.toMap(Function.identity(), value::getObjectValue);
    }

    private List<Map.Entry<String, RegistryValue>> orderEntriesBasedOnSteam(Map<String, RegistryValue> keyValuePairs) {
        return keyValuePairs.entrySet().stream().sorted((o1, o2) -> {
            String key1 = o1.getKey();
            String key2 = o2.getKey();
            if (steamOrdering().contains(key1) && steamOrdering().contains(key2)) {
                return steamOrdering().indexOf(key1) - steamOrdering().indexOf(key2);
            }
            return key1.compareTo(key2);
        }).collect(Collectors.toList());
    }

    public List<String> steamOrdering() {
        return STEAM_MAIN_SECTION_ORDERING;
    }
}
