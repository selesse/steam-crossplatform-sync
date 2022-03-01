package com.selesse.files;

import com.google.common.base.Charsets;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import mslinks.ShellLink;
import mslinks.ShellLinkException;

/**
 * File operations that wrap checked exceptions into RuntimeExceptions.
 */
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

    public static FileStore getFileStore(Path path) {
        try {
            return Files.getFileStore(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path resolveLnk(Path lnkFile) {
        try {
            if (!lnkFile.toFile().exists()) {
                throw new RuntimeException("File doesn't exist: " + lnkFile.toAbsolutePath());
            }
            return Path.of(new ShellLink(lnkFile).resolveTarget());
        } catch (IOException | ShellLinkException e) {
            throw new RuntimeException(e);
        }
    }

    public static void walkFileTree(Path base, FileVisitor<? super Path> fileVisitor) {
        try {
            Files.walkFileTree(base, fileVisitor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
