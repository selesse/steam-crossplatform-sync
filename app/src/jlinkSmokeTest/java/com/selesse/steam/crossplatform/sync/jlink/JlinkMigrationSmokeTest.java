package com.selesse.steam.crossplatform.sync.jlink;

import com.selesse.steamcrossplatformsync.gamesessions.database.Database;
import com.selesse.steamcrossplatformsync.gamesessions.database.SqliteFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Migrates a throwaway database from inside the jlink image and checks the tables landed.
 *
 * <p>Run by the {@code verifyJlinkMigrations} task, not by JUnit: the point is the packaging, so it
 * has to execute against the image's own launcher. Flyway locates migrations by scanning a
 * classpath, and a jlink image doesn't have one — the scripts sit in a {@code jrt:} filesystem. A
 * fresh install through the image once produced a database holding nothing but Flyway's schema
 * history, and no unit test can see that, because tests run from a classpath where scanning works.
 *
 * <p>Takes the directory to build the database in, rather than reading {@code XDG_DATA_HOME} or
 * {@code LOCALAPPDATA}, so it behaves the same on every OS in the CI matrix. Exits non-zero on
 * failure so the Gradle task fails with it.
 */
public class JlinkMigrationSmokeTest {
    private static final List<String> REQUIRED_TABLES = List.of("games", "gaming_sessions");

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("usage: JlinkMigrationSmokeTest <directory to build the database in>");
            System.exit(2);
        }

        Path database = Path.of(args[0]).resolve("steam-logs.sqlite3");
        Files.deleteIfExists(database);
        SqliteFile sqliteFile = new SqliteFile(database);

        // Throws when Flyway resolves no migrations at all, which is the failure this guards.
        Database.migrate(sqliteFile);

        Set<String> tables = tablesIn(sqliteFile);
        System.out.println("Tables in the fresh database: " + tables);

        List<String> missing = REQUIRED_TABLES.stream()
                .filter(table -> !tables.contains(table))
                .toList();
        if (!missing.isEmpty()) {
            System.err.println("Migrations did not apply inside the jlink image. Missing: " + missing);
            System.err.println("Flyway most likely resolved no scripts, leaving only its schema history.");
            System.exit(1);
        }

        System.out.println("jlink image migrates a fresh database correctly.");
    }

    private static Set<String> tablesIn(SqliteFile sqliteFile) throws Exception {
        Set<String> tables = new LinkedHashSet<>();
        try (Connection connection = Database.open(sqliteFile);
                Statement statement = connection.createStatement();
                ResultSet resultSet =
                        statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")) {
            while (resultSet.next()) {
                tables.add(resultSet.getString(1));
            }
        }
        return tables;
    }
}
