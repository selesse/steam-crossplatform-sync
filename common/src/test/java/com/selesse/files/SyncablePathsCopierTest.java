package com.selesse.files;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

public class SyncablePathsCopierTest {
    @Test
    public void canSyncFromPseudoHollowKnight() throws IOException {
        PatternSupportedPath fakePath1 = createTempDirectory("hollow-knight");
        PatternSupportedPath fakePattern = PatternSupportedPath.of(
                "%s/*.dat".formatted(fakePath1.toAbsolutePath().toString()));
        Path file = createTempFile(fakePath1.toAbsolutePath(), "file", ".dat");
        PatternSupportedPath fakePath2 = createTempDirectory("hollow-knight-2");

        SyncablePath source = new SyncablePath(fakePath1, fakePattern);
        SyncablePath destination = new SyncablePath(fakePath2);

        SyncablePathsCopier.copy(source, destination);

        assertThat(FileVisitors.listAll(destination.getBaseDirectory()))
                .containsExactly(Path.of(
                        fakePath2.toAbsolutePath().toString(),
                        file.getFileName().toString()));
    }

    @Test
    public void canSyncWhenThePathIsNested() throws IOException {
        PatternSupportedPath fakePath1 = createTempDirectory("hollow-knight");
        Path nestedFakePath = Path.of(fakePath1.toAbsolutePath().toString(), "/saves");
        boolean mkdirs = nestedFakePath.toFile().mkdirs();
        assert mkdirs;
        Path file = createTempFile(nestedFakePath, "file", ".dat");
        PatternSupportedPath fakePath2 = createTempDirectory("hollow-knight-2");

        PatternSupportedPath fakePattern = PatternSupportedPath.of(
                "%s/saves/*.dat".formatted(fakePath1.toAbsolutePath().toString()));
        SyncablePath source = new SyncablePath(fakePath1, fakePattern);
        SyncablePath destination = new SyncablePath(fakePath2);

        SyncablePathsCopier.copy(source, destination);

        assertThat(FileVisitors.listAll(destination.getBaseDirectory()))
                .containsExactly(Path.of(
                        fakePath2.toAbsolutePath().toString(),
                        "/saves/" + file.getFileName().toString()));
    }

    @Test
    public void willRecursivelySyncFiles_ifThePatternIsAGlob() throws IOException {
        PatternSupportedPath fakePath1 = createTempDirectory("hollow-knight");
        Path nestedFakePath = Path.of(fakePath1.toAbsolutePath().toString(), "/saves");
        boolean mkdirs = nestedFakePath.toFile().mkdirs();
        assert mkdirs;
        Path file = createTempFile(nestedFakePath, "file", ".dat");
        PatternSupportedPath fakePath2 = createTempDirectory("hollow-knight-2");

        PatternSupportedPath fakePattern = PatternSupportedPath.of(
                "%s/*".formatted(fakePath1.toAbsolutePath().toString()));
        SyncablePath source = new SyncablePath(fakePath1, fakePattern);
        SyncablePath destination = new SyncablePath(fakePath2);

        SyncablePathsCopier.copy(source, destination);

        assertThat(FileVisitors.listAll(destination.getBaseDirectory()))
                .containsExactly(Path.of(
                        fakePath2.toAbsolutePath().toString(),
                        "/saves/" + file.getFileName().toString()));
    }

    private static PatternSupportedPath createTempDirectory(String pattern) throws IOException {
        Path tempDirectory = Files.createTempDirectory(pattern);
        tempDirectory.toFile().deleteOnExit();
        return PatternSupportedPath.fromPath(tempDirectory);
    }

    private static Path createTempFile(Path directory, String name, String suffix) throws IOException {
        Path tempFile = Files.createTempFile(directory, name, suffix);
        tempFile.toFile().deleteOnExit();
        return tempFile;
    }
}
