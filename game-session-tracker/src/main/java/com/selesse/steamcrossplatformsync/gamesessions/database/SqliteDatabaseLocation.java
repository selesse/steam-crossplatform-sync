package com.selesse.steamcrossplatformsync.gamesessions.database;

import java.nio.file.Path;
import java.util.Optional;

public class SqliteDatabaseLocation {
    public static SqliteFile get() {
        String dataDirectory;
        if (System.getProperty("os.name", "").toLowerCase().contains("windows")) {
            dataDirectory = Optional.ofNullable(System.getenv("LOCALAPPDATA"))
                    .orElse(System.getProperty("user.home") + "\\AppData\\Local");
        } else {
            dataDirectory = Optional.ofNullable(System.getenv("XDG_DATA_HOME"))
                    .orElse(System.getProperty("user.home") + "/.local/share");
        }
        return new SqliteFile(Path.of(dataDirectory, "steam-crossplatform-sync", "steam-logs.sqlite3"));
    }
}
