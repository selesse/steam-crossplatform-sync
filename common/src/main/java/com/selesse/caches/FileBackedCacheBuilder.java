package com.selesse.caches;

import com.google.common.base.Splitter;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class FileBackedCacheBuilder {
    private Function<Path, Boolean> cacheLoadingCriteria;
    private Supplier<List<String>> loadingMechanism;
    private Function<List<String>, Boolean> successfulLoadCriteria;

    public FileBackedCacheBuilder setCacheLoadingCriteria(Function<Path, Boolean> cacheLoadingCriteria) {
        this.cacheLoadingCriteria = cacheLoadingCriteria;
        return this;
    }

    public FileBackedCacheBuilder setLoadingMechanism(Supplier<String> loadingMechanism) {
        this.loadingMechanism = () -> Splitter.on("\n").splitToList(loadingMechanism.get());
        return this;
    }

    public FileBackedCacheBuilder setSuccessfulLoadCriteria(Function<List<String>, Boolean> successfulLoadCriteria) {
        this.successfulLoadCriteria = successfulLoadCriteria;
        return this;
    }

    public FileBackedCache build() {
        return new FileBackedCache(cacheLoadingCriteria, loadingMechanism, successfulLoadCriteria);
    }
}
