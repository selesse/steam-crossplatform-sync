package com.selesse.processes;

import com.selesse.os.OperatingSystems;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

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
            if (OperatingSystems.get() == OperatingSystems.OperatingSystem.MAC) {
                processBuilder.environment()
                        .put("PATH", processBuilder.environment().get("PATH") + File.pathSeparator + "/usr/local/bin");
            }
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
}
