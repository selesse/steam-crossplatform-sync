package com.selesse.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class DirectoryCopier {
    public static void copyFileOrFolder(File source, File dest) throws IOException {
        if (source.isDirectory())
            copyFolder(source, dest);
        else {
            ensureParentFolder(dest);
            copyFile(source, dest);
        }
    }

    private static void copyFolder(File source, File dest, CopyOption... options) throws IOException {
        if (!dest.exists()) {
            dest.mkdirs();
        }
        File[] contents = source.listFiles();
        if (contents != null) {
            for (File file : contents) {
                File newFile = new File(dest.getAbsolutePath() + File.separator + file.getName());
                if (file.isDirectory()) {
                    copyFolder(file, newFile);
                }
                else {
                    copyFile(file, newFile);
                }
            }
        }
    }

    private static void copyFile(File source, File dest) throws IOException {
        if (dest.exists()) {
            dest.delete();
        }
        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
    }

    private static void ensureParentFolder(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }
}
