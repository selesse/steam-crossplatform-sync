package com.selesse.steam.appcache;

import java.util.ArrayList;
import java.util.List;

public class VdfObject {
    private final String name;
    private final List<Object> values;

    public VdfObject(String name) {
        this.name = name;
        this.values = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void add(VdfInteger vdfIntValue) {
        this.values.add(vdfIntValue);
    }

    public void add(VdfObject vdf) {
        this.values.add(vdf);
    }

    public void add(VdfString vdf) {
        this.values.add(vdf);
    }

    public List<Object> getValues() {
        return values;
    }
}
