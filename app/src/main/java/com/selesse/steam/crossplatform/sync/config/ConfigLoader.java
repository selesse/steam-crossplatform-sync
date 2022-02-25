package com.selesse.steam.crossplatform.sync.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.selesse.steam.crossplatform.sync.serialize.ConfigRaw;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private static Optional<ConfigRaw> config = null;

    public static Optional<ConfigRaw> loadIfExists(Path configLocation) {
        if (config == null) {
            config = loadMaybeForReal(configLocation);
        }
        return config;
    }

    private static Optional<ConfigRaw> loadMaybeForReal(Path configLocation) {
        var mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        try {
            // e.g. read the local config, otherwise pick a sane default
            var rawConfig = mapper.readValue(configLocation.toFile(), ConfigRaw.class);
            return Optional.ofNullable(rawConfig);
        } catch (FileNotFoundException e) {
            logger.debug("Config not found {}", configLocation);
            return Optional.empty();
        } catch (IOException e) {
            logger.warn("Couldn't parse config {}", configLocation);
            return Optional.empty();
        }
    }
}
