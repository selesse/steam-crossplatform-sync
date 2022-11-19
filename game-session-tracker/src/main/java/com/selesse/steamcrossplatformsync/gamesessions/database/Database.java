package com.selesse.steamcrossplatformsync.gamesessions.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.flywaydb.core.Flyway;

public class Database {
    private static boolean initialized = false;

    public static void prepare(SqliteFile sqliteFile) {
        File parentFile = sqliteFile.path().getParent().toFile();
        if (!parentFile.exists()) {
            boolean mkdirs = parentFile.mkdirs();
            assert (mkdirs);
        }

        var flyway =
                Flyway.configure().dataSource(sqliteFile.jdbcPath(), "", "").load();
        flyway.migrate();
    }

    public static Connection getConnection() throws SQLException {
        return getConnection(SqliteDatabaseLocation.get());
    }

    public static Connection getConnection(SqliteFile sqliteFile) throws SQLException {
        if (!initialized) {
            prepare(sqliteFile);
            initialized = true;
        }

        return DriverManager.getConnection(sqliteFile.jdbcPath());
    }
}
