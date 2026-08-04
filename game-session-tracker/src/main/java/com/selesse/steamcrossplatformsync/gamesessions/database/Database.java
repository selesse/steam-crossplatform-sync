package com.selesse.steamcrossplatformsync.gamesessions.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
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

        var configuration = Flyway.configure().dataSource(sqliteFile.jdbcPath(), "", "");
        // Where the module can list its own scripts, hand them over rather than let Flyway scan
        // for them; scanning finds nothing inside a jlink image. There are no Java migrations, so
        // the empty class provider spares Flyway a second futile scan.
        ModuleMigrationScripts.forThisModule()
                .ifPresent(scripts -> configuration.resourceProvider(scripts).javaMigrationClassProvider(List::of));

        var flyway = configuration.load();
        // Flyway treats "I found no migration scripts at all" as nothing to do, so a packaging
        // that hides db/migration from it produces an empty database and carries on. That only
        // surfaces later, as a missing table when the first session is recorded.
        if (flyway.info().all().length == 0) {
            throw new IllegalStateException("Flyway resolved no migrations for " + sqliteFile.path()
                    + ". The scripts under db/migration aren't reachable from Flyway in this"
                    + " packaging, so the database would be left with no tables.");
        }
        flyway.migrate();
    }

    /** Opens a connection. Callers are responsible for having migrated the file first. */
    public static Connection open(SqliteFile sqliteFile) throws SQLException {
        return DriverManager.getConnection(sqliteFile.jdbcPath());
    }
}
