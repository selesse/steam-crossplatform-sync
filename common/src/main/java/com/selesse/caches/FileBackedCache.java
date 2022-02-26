package com.selesse.caches;

import com.selesse.files.RuntimeExceptionFiles;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class FileBackedCache {
    private final Function<Path, Boolean> cacheLoadingCriteria;
    private final Supplier<List<String>> loadingMechanism;

    FileBackedCache(Function<Path, Boolean> cacheLoadingCriteria, Supplier<List<String>> loadingMechanism) {
        this.cacheLoadingCriteria = cacheLoadingCriteria;
        this.loadingMechanism = loadingMechanism;
    }

    public List<String> getLines(Path path) {
        boolean shouldReadCache = cacheLoadingCriteria.apply(path);
        if (shouldReadCache) {
            return RuntimeExceptionFiles.readAllLines(path);
        }
        List<String> lines = loadingMechanism.get();
        RuntimeExceptionFiles.writeString(path, String.join("\n", lines));
        return lines;
    }
}
