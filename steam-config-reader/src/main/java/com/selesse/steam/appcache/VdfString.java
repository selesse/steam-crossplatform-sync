package com.selesse.steam.appcache;

import java.util.Objects;

public class VdfString {
    private final String name;
    private final String value;

    public VdfString(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VdfString vdfString = (VdfString) o;
        return Objects.equals(name, vdfString.name) && Objects.equals(value, vdfString.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }
}
