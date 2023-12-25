package com.selesse.os;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

public class Hostnames {
    public static String getCurrent() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return Optional.ofNullable(System.getenv("COMPUTERNAME"))
                    .orElseGet(
                            () -> Optional.ofNullable(System.getenv("HOSTNAME")).orElse(fallbackHostname()));
        }
    }

    private static String fallbackHostname() {
        try {
            Process hostnameProcess = new ProcessBuilder("hostname").start();
            return new BufferedReader(new InputStreamReader(hostnameProcess.getInputStream())).readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
