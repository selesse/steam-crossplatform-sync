package com.selesse.os;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

public class Resources {
    public static Path getResource(String name) {
        URL resource = Resources.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new RuntimeException("Could not find resource \"" + name + "\" on classpath");
        }
        try {
            return Path.of(resource.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean exists(String name) {
        URL resource = Resources.class.getClassLoader().getResource(name);
        return resource != null;
    }
}
