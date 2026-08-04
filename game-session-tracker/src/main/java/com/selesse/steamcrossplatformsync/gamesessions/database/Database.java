package com.selesse.steamcrossplatformsync.gamesessions.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.flywaydb.core.Flyway;

public class Database {
    /**
     * Brings {@code sqliteFile} up to the current schema, creating the file if it doesn't exist
     * yet. Flyway is idempotent, so migrating the same file twice is harmless.
     */
    public static void migrate(SqliteFile sqliteFile) {
        File parentFile = sqliteFile.path().getParent().toFile();
        if (!parentFile.exists()) {
            boolean mkdirs = parentFile.mkdirs();
            assert (mkdirs);
        }

        var flyway =
                Flyway.configure().dataSource(sqliteFile.jdbcPath(), "", "").load();
        flyway.migrate();
    }

    /** Opens a connection. Callers are responsible for having migrated the file first. */
    public static Connection open(SqliteFile sqliteFile) throws SQLException {
        return DriverManager.getConnection(sqliteFile.jdbcPath());
    }
}
