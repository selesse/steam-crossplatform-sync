package com.selesse.steam.appcache;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

// Not a record: it needs a genuinely mutable field (the memoized decoded value), which records
// can't declare - a record's only instance state is its components.
public final class VdfString {
    private final String name;
    private final byte[] data;
    private final int offset;
    private final int length;
    private String value;

    public VdfString(String name, String value) {
        this.name = name;
        this.data = null;
        this.offset = 0;
        this.length = 0;
        this.value = value;
    }

    VdfString(String name, byte[] data, int offset, int length) {
        this.name = name;
        this.data = data;
        this.offset = offset;
        this.length = length;
    }

    public String name() {
        return name;
    }

    public String value() {
        if (value == null) {
            value = new String(data, offset, length, StandardCharsets.UTF_8);
        }
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VdfString other)) {
            return false;
        }
        return Objects.equals(name, other.name) && Objects.equals(value(), other.value());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value());
    }

    @Override
    public String toString() {
        return "VdfString[name=" + name + ", value=" + value() + "]";
    }
}
