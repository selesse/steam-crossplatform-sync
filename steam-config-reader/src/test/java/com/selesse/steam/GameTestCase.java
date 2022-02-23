package com.selesse.steam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.selesse.os.Resources;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GameTestCase {
    public String name;
    public String windows;
    public String mac;
    public String linux;

    public GameTestCase() {
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
        return testCases.games;
    }
}
