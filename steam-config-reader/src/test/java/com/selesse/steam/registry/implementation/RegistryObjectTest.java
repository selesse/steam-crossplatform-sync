package com.selesse.steam.registry.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.Test;

public class RegistryObjectTest {
    @Test
    public void pathExistsFindsANestedString() {
        RegistryObject registry = appWithCommonBlock();

        assertThat(registry.pathExists("common/name")).isTrue();
        assertThat(registry.pathExists("common")).isTrue();
    }

    @Test
    public void pathExistsIsFalseForAMissingSegment() {
        RegistryObject registry = appWithCommonBlock();

        assertThat(registry.pathExists("common/nope")).isFalse();
        assertThat(registry.pathExists("nope")).isFalse();
    }

    @Test
    public void pathExistsIsFalseWhenAMidPathSegmentIsAString() {
        RegistryObject registry = appWithCommonBlock();

        // "common/name" is a string, so it has no "type" underneath it. The sibling "common/type"
        // existing must not make this path resolve.
        assertThat(registry.pathExists("common/name/type")).isFalse();
    }

    @Test
    public void lookupsDoNotIgnoreTrailingSegmentsPastAString() {
        RegistryObject registry = appWithCommonBlock();

        assertThat(registry.findString("common/name/type")).isEmpty();
        assertThatThrownBy(() -> registry.getObjectValueAsString("common/name/type"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("common/name/type");
    }

    @Test
    public void asObjectRejectsAStringInsteadOfClassCasting() {
        RegistryObject registry = appWithCommonBlock();

        assertThatThrownBy(() -> registry.getObjectValueAsObject("common/name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("common/name");
    }

    @Test
    public void findersAreEmptyRatherThanNullForMissingPaths() {
        RegistryObject registry = appWithCommonBlock();

        assertThat(registry.findObject("ufs")).isEmpty();
        assertThat(registry.findString("common/nope")).isEmpty();
        assertThat(registry.findObject("common")).isPresent();
        assertThat(registry.findString("common/name")).isPresent();
    }

    private RegistryObject appWithCommonBlock() {
        return RegistryParser.parseWithoutRegistryCollapse(List.of(
                "\"common\"",
                "{",
                "\t\"gameid\"\t\"9999991\"",
                "\t\"name\"\t\"Test Game\"",
                "\t\"type\"\t\"Game\"",
                "}"));
    }
}
