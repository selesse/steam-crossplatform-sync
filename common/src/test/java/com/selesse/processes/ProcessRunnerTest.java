package com.selesse.processes;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.os.OperatingSystems;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import org.junit.Test;

public class ProcessRunnerTest {

    @Test
    public void malformedPathEntryIsSkippedRatherThanThrowing() throws IOException {
        Path tempDir = Files.createTempDirectory("process-runner-test");
        tempDir.toFile().deleteOnExit();
        String programName = "my-program";
        Path programFile = tempDir.resolve(programName);
        Files.writeString(programFile, "");
        programFile.toFile().deleteOnExit();
        if (OperatingSystems.get() != OperatingSystems.OperatingSystem.WINDOWS) {
            Files.setPosixFilePermissions(
                    programFile,
                    EnumSet.of(
                            PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE,
                            PosixFilePermission.OWNER_EXECUTE));
        }

        // A NUL byte makes for a path string that's invalid on every platform, simulating a
        // corrupted $PATH entry (e.g. a stray quote on Windows) without relying on OS-specific
        // invalid characters.
        String malformedEntry = "\0this-is-not-a-valid-path";
        String pathEnvironmentVariable = malformedEntry + File.pathSeparator + tempDir;

        ProcessRunner processRunner = new ProcessRunner(programName);
        Path resolved = processRunner.findAbsolutePathOfProgramInPath(programName, pathEnvironmentVariable);

        assertThat(resolved).exists();
    }
}
