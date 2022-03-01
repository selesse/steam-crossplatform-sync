package com.selesse.files;

import java.nio.file.Path;
import java.util.List;

public class FileVisitors {
    public static List<Path> listAll(Path base) {
        PathMatcherFileVisitor fileVisitor = new PathMatcherFileVisitor((x) -> true);
        RuntimeExceptionFiles.walkFileTree(base, fileVisitor);
        return fileVisitor.getMatchingPaths();
    }
}
