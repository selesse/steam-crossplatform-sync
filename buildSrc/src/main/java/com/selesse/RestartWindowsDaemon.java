package com.selesse;

import org.gradle.api.Project;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Stream;

class RestartWindowsDaemon {
    private final Project project;
    private final File shadowJarPath;

    public RestartWindowsDaemon(Project project, File shadowJarPath) {
        this.project = project;
        this.shadowJarPath = shadowJarPath;
    }

    public void restart() throws IOException, InterruptedException {
        killExistingDaemon();
        startNewDaemon();
    }

    private void killExistingDaemon() throws IOException, InterruptedException {
        String javawProcesses = getJavawProcesses();

        Stream.of(javawProcesses.split("\n"))
                .filter(x -> x.contains(shadowJarPath.getName()))
                .map(line -> line.split(" ")[line.split(" ").length - 1])
                .forEach(pid -> ProcessHandle.of(Long.parseLong(pid)).ifPresent(ProcessHandle::destroy));
    }

    private String getJavawProcesses() throws IOException, InterruptedException {
        ProcessBuilder processBuilder =
                new ProcessBuilder("wmic", "process", "where", "name=\"javaw.exe\"", "get", "CommandLine,Processid");
        Process process = processBuilder.start();
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = input.readLine()) != null) {
            output.append(line).append("\n");
        }
        process.waitFor();
        return output.toString();
    }

    private void startNewDaemon() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder().command(
                JavaHomeLocator.locate(project) + File.separator + "bin" + File.separator + "javaw.exe",
                "-jar",
                shadowJarPath.getAbsolutePath()
        );
        Process process = processBuilder.start();
        System.out.println("Started new daemon, pid=" + process.pid());
    }
}
