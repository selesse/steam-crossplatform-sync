package com.selesse.steam.crossplatform.sync.drive;

import com.selesse.os.OperatingSystems;

import java.nio.file.Path;
import java.sql.*;
import java.util.Optional;

public class GoogleDrive {
    public static Optional<Path> getDriveRoot() {
        if (OperatingSystems.get() == OperatingSystems.OperatingSystem.MAC) {
            Path file = Path.of(
                    System.getProperty("user.home"), "Library", "Application Support", "Google", "Drive", "user_default", "sync_config.db"
            );

            try {
                Connection connectionToDb = getConnectionToDb(file);
                Statement connection = connectionToDb.createStatement();
                connection.execute("select data_value from data where entry_key = \"local_sync_root_path\"");
                ResultSet resultSet = connection.getResultSet();
                String result = resultSet.getString(1);
                return Optional.of(Path.of(result));
            } catch (SQLException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static Connection getConnectionToDb(Path dbPath) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath.toAbsolutePath().toString());
    }
}
