package com.selesse.steamcrossplatformsync.gamesessions.database;

import com.selesse.os.OperatingSystems;
import java.nio.file.Path;
import java.util.Optional;

public class SqliteDatabaseLocation {
    public static SqliteFile get() {
        return new SqliteFile(
                dataDirectory().resolve("steam-crossplatform-sync").resolve("steam-logs.sqlite3"));
    }

    /**
     * Where this machine keeps application data - deliberately not where it keeps configuration, so
     * a session history isn't something you'd think to copy between machines along with config.yml.
     */
    private static Path dataDirectory() {
        Path home = Path.of(System.getProperty("user.home"));
        return switch (OperatingSystems.get().family()) {
            case WINDOWS ->
                environmentPath("LOCALAPPDATA")
                        .orElseGet(() -> home.resolve("AppData").resolve("Local"));
            case MAC, LINUX ->
                environmentPath("XDG_DATA_HOME")
                        .orElseGet(() -> home.resolve(".local").resolve("share"));
        };
    }

    private static Optional<Path> environmentPath(String name) {
        return Optional.ofNullable(System.getenv(name))
                .filter(value -> !value.isEmpty())
                .map(Path::of);
    }
}
