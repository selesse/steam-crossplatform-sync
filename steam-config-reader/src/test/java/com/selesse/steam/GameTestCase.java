package com.selesse.steam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.selesse.os.Resources;
import com.selesse.steam.games.SteamAccountPathReplacer;
import java.io.File;
import java.io.IOException;
import java.util.List;

public record GameTestCase(String name, List<String> windows, List<String> mac, List<String> linux) {
    public List<String> windowsPath() {
        return windows().stream()
                .map(x -> new SteamAccountPathReplacer().replace(x))
                .toList();
    }

    public List<String> macPath() {
        return mac().stream()
                .map(x -> new SteamAccountPathReplacer().replace(x))
                .toList();
    }

    public List<String> linuxPath() {
        return linux().stream()
                .map(x -> new SteamAccountPathReplacer().replace(x))
                .toList();
    }

    @Override
    public String toString() {
        return name;
    }

    public static List<GameTestCase> load() throws IOException {
        TestAppCache.setup();
        File source = Resources.getResource("game-installation-paths.yml").toFile();
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        var testCases = objectMapper.readValue(source, GameTestCases.class);
        return testCases.games();
    }
}
