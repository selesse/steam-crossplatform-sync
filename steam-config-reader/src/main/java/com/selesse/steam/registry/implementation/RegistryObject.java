package com.selesse.steam.registry.implementation;

import com.selesse.text.Splitter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class RegistryObject implements RegistryValue {
    private final Map<String, RegistryValue> values;

    public RegistryObject() {
        this.values = new LinkedHashMap<>();
    }

    public RegistryValue get(String part) {
        // Deal with registry lookups in case insensitive ways (e.g. for OS X)
        if (values.containsKey(part.toLowerCase())) {
            return values.get(part.toLowerCase());
        }
        return values.get(part);
    }

    public List<String> getKeys() {
        return List.copyOf(values.keySet());
    }

    public void put(String key, RegistryValue value) {
        values.put(key, value);
    }

    /** The nested block at {@code path}, or empty if nothing is there or it resolves to a string. */
    public Optional<RegistryObject> findObject(String path) {
        return find(path).filter(RegistryObject.class::isInstance).map(RegistryObject.class::cast);
    }

    /** The leaf string at {@code path}, or empty if nothing is there or it resolves to a block. */
    public Optional<RegistryString> findString(String path) {
        return find(path).filter(RegistryString.class::isInstance).map(RegistryString.class::cast);
    }

    /** The block at {@code path}, which the caller is asserting is there. */
    public RegistryObject getObjectValueAsObject(String path) {
        return findObject(path).orElseThrow(() -> missing(path, "a block"));
    }

    /** The string at {@code path}, which the caller is asserting is there. */
    public RegistryString getObjectValueAsString(String path) {
        return findString(path).orElseThrow(() -> missing(path, "a string"));
    }

    public boolean pathExists(String path) {
        return find(path).isPresent();
    }

    /**
     * Walks {@code path} one {@code /}-separated segment at a time.
     *
     * <p>Hitting a string with segments still to go means the path doesn't exist: a leaf has
     * nothing underneath it, and resolving the rest against the last block we were in would happily
     * match a sibling of the string instead.
     */
    private Optional<RegistryValue> find(String path) {
        if (values.containsKey(path)) {
            return Optional.ofNullable(values.get(path));
        }
        List<String> parts = Splitter.on("/").splitToList(path);

        RegistryValue current = this;
        for (String part : parts) {
            if (!(current instanceof RegistryObject currentObject)) {
                return Optional.empty();
            }
            current = currentObject.get(part);
            if (current == null) {
                return Optional.empty();
            }
        }
        return Optional.of(current);
    }

    private IllegalArgumentException missing(String path, String expected) {
        return new IllegalArgumentException(
                "Expected %s at \"%s\", but the registry has %s".formatted(expected, path, describeWhatIsThere(path)));
    }

    private String describeWhatIsThere(String path) {
        return find(path)
                .map(value -> switch (value) {
                    case RegistryObject ignored -> "a block";
                    case RegistryString ignored -> "a string";
                })
                .orElse("nothing");
    }
}
