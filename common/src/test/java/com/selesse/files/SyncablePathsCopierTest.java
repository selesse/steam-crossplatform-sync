package com.selesse.files;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

public class SyncablePathsCopierTest {
    @Test
    public void canSyncFromPseudoHollowKnight() throws IOException {
        Path fakePath1 = createTempDirectory("hollow-knight");
        Path fakePattern = Path.of(fakePath1.toAbsolutePath().toString(), "*.dat");
        Path file = createTempFile(fakePath1, "file", ".dat");
        Path fakePath2 = createTempDirectory("hollow-knight-2");

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
        Path fakePath1 = createTempDirectory("hollow-knight");
        Path nestedFakePath = Path.of(fakePath1.toAbsolutePath().toString(), "/saves");
        boolean mkdirs = nestedFakePath.toFile().mkdirs();
        assert mkdirs;
        Path file = createTempFile(nestedFakePath, "file", ".dat");
        Path fakePath2 = createTempDirectory("hollow-knight-2");

        Path fakePattern = Path.of(fakePath1.toAbsolutePath().toString(), "/saves/*.dat");
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
        Path fakePath1 = createTempDirectory("hollow-knight");
        Path nestedFakePath = Path.of(fakePath1.toAbsolutePath().toString(), "/saves");
        boolean mkdirs = nestedFakePath.toFile().mkdirs();
        assert mkdirs;
        Path file = createTempFile(nestedFakePath, "file", ".dat");
        Path fakePath2 = createTempDirectory("hollow-knight-2");

        Path fakePattern = Path.of(fakePath1.toAbsolutePath().toString(), "/*");
        SyncablePath source = new SyncablePath(fakePath1, fakePattern);
        SyncablePath destination = new SyncablePath(fakePath2);

        SyncablePathsCopier.copy(source, destination);

        assertThat(FileVisitors.listAll(destination.getBaseDirectory()))
                .containsExactly(Path.of(
                        fakePath2.toAbsolutePath().toString(),
                        "/saves/" + file.getFileName().toString()));
    }

    private static Path createTempDirectory(String pattern) throws IOException {
        Path tempDirectory = Files.createTempDirectory(pattern);
        tempDirectory.toFile().deleteOnExit();
        return tempDirectory;
    }

    private static Path createTempFile(Path directory, String name, String suffix) throws IOException {
        Path tempDirectory = Files.createTempFile(directory, name, suffix);
        tempDirectory.toFile().deleteOnExit();
        return tempDirectory;
    }
}
