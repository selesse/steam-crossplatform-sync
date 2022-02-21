package com.selesse;

import java.io.IOException;

class RestartMacDaemon {
    public void restart() throws IOException, InterruptedException {
        stopExisting();
        startDaemon();
    }

    private void stopExisting() throws InterruptedException, IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("launchctl", "unload", getPlistPath());
        Process process = processBuilder.start();
        process.waitFor();
    }

    private void startDaemon() throws IOException, InterruptedException {
        System.out.println("Loading daemon");
        ProcessBuilder processBuilder = new ProcessBuilder("launchctl", "load", getPlistPath());
        Process process = processBuilder.start();
        process.waitFor();
    }

    private String getPlistPath() {
        return System.getProperty("user.home") + "/Library/LaunchAgents/com.selesse.steam-crossplatform-sync.plist";
    }
}
