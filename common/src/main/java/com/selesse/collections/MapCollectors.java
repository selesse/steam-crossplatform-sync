package com.selesse.collections;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class MapCollectors {
    public static <T, K, U> Collector<T, ?, Map<K, U>> toLinkedMap(
            Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(
                keyMapper,
                valueMapper,
                (u, v) -> {
                    throw new IllegalStateException("Duplicate key " + u);
                },
                LinkedHashMap::new);
    }
}
