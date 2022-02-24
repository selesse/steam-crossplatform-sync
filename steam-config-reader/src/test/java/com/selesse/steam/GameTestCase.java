package com.selesse.steam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.selesse.os.Resources;
import com.selesse.steam.games.SteamAccountPathReplacer;

import java.io.File;
import java.io.IOException;
import java.util.List;

public record GameTestCase(String name, String windows, String mac, String linux) {
    public String windowsPath() {
        return new SteamAccountPathReplacer().replace(windows());
    }
    public String macPath() {
        return new SteamAccountPathReplacer().replace(mac());
    }
    public String linuxPath() {
        return new SteamAccountPathReplacer().replace(linux());
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
