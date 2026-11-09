package com.selesse.steam.streaming;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/** Reads every Remote Play stream recorded in a Steam {@code logs} directory, in either role. */
public final class RemotePlayLogs {
    private static final String HOST_LOG = "streaming_log";
    private static final String CLIENT_LOG = "webhelper_js";

    private RemotePlayLogs() {}

    public static List<RemotePlayStream> read(Path logsDirectory, ZoneId zone) {
        List<RemotePlayStream> streams = new ArrayList<>();
        streams.addAll(RemotePlayLogParser.hostStreams(readRotated(logsDirectory, HOST_LOG), zone));
        streams.addAll(RemotePlayLogParser.clientStreams(readRotated(logsDirectory, CLIENT_LOG), zone));
        return streams;
    }

    // Steam rotates a log into <name>.previous.txt. Reading the older file first lets a stream that
    // straddles the rotation be seen whole.
    private static List<String> readRotated(Path logsDirectory, String name) {
        List<String> lines = new ArrayList<>();
        lines.addAll(readIfPresent(logsDirectory.resolve(name + ".previous.txt")));
        lines.addAll(readIfPresent(logsDirectory.resolve(name + ".txt")));
        return lines;
    }

    // Decoded leniently: these logs quote whatever a game or persona name contains, and one bad byte
    // shouldn't cost every stream in the file.
    private static List<String> readIfPresent(Path path) {
        if (!Files.isRegularFile(path)) {
            return List.of();
        }
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8)
                    .lines()
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
