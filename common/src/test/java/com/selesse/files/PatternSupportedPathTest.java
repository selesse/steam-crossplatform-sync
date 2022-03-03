package com.selesse.files;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.Before;
import org.junit.Test;

public class PatternSupportedPathTest {
    private String base;

    @Before
    public void setup() {
        base = OsAgnosticPaths.of(System.getProperty("user.home"));
    }

    @Test
    public void canHandleGlobs() {
        PatternSupportedPath path = PatternSupportedPath.of(base + "/*.txt");
        assertThat(path.toGlobPath()).isEqualTo("glob:%s/*.txt".formatted(base));
    }

    @Test
    public void canHandleRecursiveGlobs() {
        PatternSupportedPath path = PatternSupportedPath.of(base + "/*");
        assertThat(path.toGlobPath()).isEqualTo("glob:%s/**".formatted(base));
    }

    @Test
    public void returnsTheParentDirectory_ifItIsAnAsterisk() {
        PatternSupportedPath path = PatternSupportedPath.of(base + "/*");
        assertThat(path.toAbsolutePath()).isEqualTo(Path.of(base));
    }

    @Test
    public void getParentReturnsTheBaseDirectory_ifItContainsAnAsterisk() {
        PatternSupportedPath path = PatternSupportedPath.of(base + "/*");
        assertThat(path.getParent().toAbsolutePath()).isEqualTo(Path.of(base).toAbsolutePath());
    }

    @Test
    public void getParentReturnsTheParentDirectory_ifItDoesNotContainAnAsterisk() {
        PatternSupportedPath path = PatternSupportedPath.of(base + "/bob");
        assertThat(path.getParent().toAbsolutePath()).isEqualTo(Path.of(base).toAbsolutePath());
    }
}
