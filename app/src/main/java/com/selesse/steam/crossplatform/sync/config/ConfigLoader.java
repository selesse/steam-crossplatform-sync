package com.selesse.steam.crossplatform.sync.config;

import com.selesse.steam.crossplatform.sync.serialize.ConfigRaw;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

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
        if (!Files.exists(configLocation)) {
            logger.debug("Config not found {}", configLocation);
            return Optional.empty();
        }
        var mapper = new ObjectMapper(new YAMLFactory());
        try {
            // e.g. read the local config, otherwise pick a sane default
            var rawConfig = mapper.readValue(configLocation.toFile(), ConfigRaw.class);
            return Optional.ofNullable(rawConfig);
        } catch (JacksonException e) {
            logger.warn("Couldn't parse config {}", configLocation);
            return Optional.empty();
        }
    }
}
