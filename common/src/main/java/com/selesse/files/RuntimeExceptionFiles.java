package com.selesse.files;

import com.google.common.base.Charsets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RuntimeExceptionFiles {
    public static void writeString(Path path, String string) {
        File parentDirectory = path.toFile().getParentFile();
        if (!parentDirectory.isDirectory()) {
            boolean mkdirs = parentDirectory.mkdirs();
            if (!mkdirs) {
                throw new RuntimeException("Unable to create directory " + parentDirectory);
            }
        }
        try {
            Files.writeString(path, string, Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write to " + path, e);
        }
    }

    public static List<String> readAllLines(Path path) {
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
