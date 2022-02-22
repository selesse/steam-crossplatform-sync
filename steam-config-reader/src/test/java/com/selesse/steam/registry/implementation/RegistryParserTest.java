package com.selesse.steam.registry.implementation;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RegistryParserTest {
    @Test
    public void canParseRegistryWithSpecialCharacter() {
        String specialKeyName = String.format("\"%s\"", "hello\t");
        String specialValue = String.format("\"%s\"", "my \tfriend\\\"");
        List<String> lines = List.of(String.format("\t\t%s\t\t%s", specialKeyName, specialValue));
        RegistryStore registry = RegistryParser.parse(lines);

        RegistryValue objectValue = registry.getObjectValue("hello\t");
        assertThat(objectValue).isInstanceOf(RegistryString.class);

        assertThat(((RegistryString) objectValue).getName()).isEqualTo("hello\t");
        assertThat(((RegistryString) objectValue).getValue()).isEqualTo("my \tfriend\\\"");
    }
}