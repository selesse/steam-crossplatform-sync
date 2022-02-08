package com.selesse.processes;

import com.selesse.os.OperatingSystems;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ProcessRunner {
    private final ProcessBuilder processBuilder;

    public ProcessRunner(String... args) {
        processBuilder = new ProcessBuilder(args);
    }

    public ProcessRunner(List<String> args) {
        processBuilder = new ProcessBuilder(args);
    }

    public String runAndGetOutput() {
        try {
            replaceRelativePathWithAbsolutePath();
            Process process = processBuilder.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = input.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
            return output.toString();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Java's ProcessBuilder doesn't let you modify the $PATH for the existing execution, so we have to manually look
    // up the executable on the $PATH and replace it with its absolute path.
    // On OS X, steamcmd wasn't being found since it was in /usr/local/bin and that's not on the default plist PATH...
    private void replaceRelativePathWithAbsolutePath() {
        List<String> existingArguments = processBuilder.command();
        String programName = existingArguments.get(0);
        String pathEnvironmentVariable;
        if (OperatingSystems.get() != OperatingSystems.OperatingSystem.WINDOWS) {
            pathEnvironmentVariable = System.getenv("PATH") + File.pathSeparator + "/usr/local/bin";
        } else {
            String pathVariable =
                    System.getenv().keySet().stream().filter(x -> x.equalsIgnoreCase("path")).findFirst().orElseThrow();
            pathEnvironmentVariable = System.getenv(pathVariable);
        }
        Path filePath = findAbsolutePathOfProgramInPath(programName, pathEnvironmentVariable);
        existingArguments.set(0, filePath.toString());
    }

    private Path findAbsolutePathOfProgramInPath(String programName, String pathEnvironmentVariable) {
        return Stream.of(pathEnvironmentVariable.split(Pattern.quote(File.pathSeparator)))
                .map(Paths::get)
                .map(path -> {
                    if (OperatingSystems.get() == OperatingSystems.OperatingSystem.WINDOWS) {
                        return prioritizeExeProgramNameIfExists(programName, path);
                    } else {
                        return Path.of(path.toString(), programName);
                    }
                })
                .filter(path -> Files.exists(path) && Files.isExecutable(path))
                .findFirst().orElseThrow(() -> new RuntimeException("Could not find " + programName + " in $PATH"));
    }

    private Path prioritizeExeProgramNameIfExists(String programName, Path path) {
        Path pathWithExe = Path.of(path.toString(), programName + ".exe");
        Path pathWithoutExe = Path.of(path.toString(), programName);
        if (Files.exists(pathWithExe)) {
            return pathWithExe;
        } else {
            return pathWithoutExe;
        }
    }
}
