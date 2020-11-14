package com.selesse.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class DirectoryCopier {
    public static void recursiveCopy(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            copyFolder(source, destination);
        } else {
            createParentFolderIfNecessary(destination);
            copyFile(source, destination);
        }
    }

    private static void copyFolder(File source, File destination) throws IOException {
        if (!destination.exists()) {
            destination.mkdirs();
        }
        File[] contents = source.listFiles();
        if (contents != null) {
            for (File file : contents) {
                File newFile = new File(destination.getAbsolutePath() + File.separator + file.getName());
                if (file.isDirectory()) {
                    copyFolder(file, newFile);
                } else {
                    copyFile(file, newFile);
                }
            }
        }
    }

    private static void copyFile(File source, File destination) throws IOException {
        if (destination.exists()) {
            destination.delete();
        }
        Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
    }

    private static void createParentFolderIfNecessary(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }
}
