package com.selesse.steamcrossplatformsync.gamesessions.database;

import java.nio.file.Path;

public record SqliteFile(Path path) {
    String jdbcPath() {
        return "jdbc:sqlite:/" + path();
    }
}
