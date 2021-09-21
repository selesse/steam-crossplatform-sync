package com.selesse.steam.steamcmd;

import com.google.common.collect.Lists;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * I wish this didn't have to exist, but I couldn't find any other way to do this.
 *
 * Basically, steamcmd doesn't give you a way to request info about an app
 * you don't have locally. The one odd workaround that I noticed works
 * was to do `app_info_print` twice in a row in an interactive session.
 * The first time doesn't work, but if you wait a bit and do it again,
 * it'll print the app info. That's the only way I've found to trigger
 * the actual update. My hope is that one day, `app_info_update` will
 * work and this won't be needed.
 *
 * This horrible class simulates doing it interactively, and captures the second
 * `app_info_print`'s output.
 */
class AppInfoPrintInteractive {
    public List<String> runPrintAppInfoProcess(Long appId) {
        ProcessBuilder processBuilder = new ProcessBuilder("steamcmd", "+login", "anonymous");

        List<String> lines = Lists.newArrayList();
        boolean startCapturing = false;

        try {
            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("Connecting anonymously to Steam Public...OK")) {
                    try (PrintStream printStream = new PrintStream(process.getOutputStream())) {
                        printStream.println();
                        printStream.flush();

                        while ((line = bufferedReader.readLine()) != null) {
                            if (line.startsWith("Steam>")) {
                                printStream.println("app_info_print " + appId);
                                printStream.flush();

                                TimeUnit.MILLISECONDS.sleep(500);

                                printStream.println("app_info_print " + appId);
                                printStream.flush();
                                while ((line = bufferedReader.readLine()) != null) {
                                    if (line.startsWith("\"" + appId + "\"")) {
                                        startCapturing = true;
                                    }
                                    if (startCapturing) {
                                        lines.add(line);
                                    }
                                    if (line.startsWith("}")) {
                                        printStream.println("quit");
                                        printStream.flush();
                                        break;
                                    }
                                }
                                return lines;
                            }
                        }
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return lines;
    }
}
