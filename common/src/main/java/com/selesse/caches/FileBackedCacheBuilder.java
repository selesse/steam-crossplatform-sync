package com.selesse.caches;

import com.google.common.base.Splitter;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class FileBackedCacheBuilder {
    private Function<Path, Boolean> cacheLoadingCriteria;
    private Supplier<List<String>> loadingMechanism;

    public FileBackedCacheBuilder setCacheLoadingCriteria(Function<Path, Boolean> cacheLoadingCriteria) {
        this.cacheLoadingCriteria = cacheLoadingCriteria;
        return this;
    }

    public FileBackedCacheBuilder setLoadingMechanism(Supplier<String> loadingMechanism) {
        this.loadingMechanism = () -> Splitter.on("\n").splitToList(loadingMechanism.get());
        return this;
    }

    public FileBackedCache build() {
        return new FileBackedCache(cacheLoadingCriteria, loadingMechanism);
    }
}
