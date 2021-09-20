package com.selesse.steam.crossplatform.sync.cloud;

import com.selesse.os.OperatingSystems;

import java.nio.file.Path;
import java.sql.*;
import java.util.Optional;
import java.util.stream.Stream;

public class GoogleDrive {
    public static Optional<Path> getDriveRoot() {
        Path localDbPath = switch (OperatingSystems.get()) {
            case MAC -> defaultMacPath();
            case WINDOWS -> defaultWindowsPath();
        };

        try {
            Connection connectionToDb = getConnectionToDb(localDbPath);
            Statement connection = connectionToDb.createStatement();
            connection.execute("select data_value from data where entry_key = \"local_sync_root_path\"");
            ResultSet resultSet = connection.getResultSet();
            String result = resultSet.getString(1);
            // Not sure why, but on Windows it's prefixed with this
            if (result.startsWith("\\\\?\\")) {
                result = result.replace("\\\\?\\", "");
            }
            return Optional.of(Path.of(result));
        } catch (SQLException ignored) {
            return Stream.of(defaultPath(), reasonableRename())
                    .filter(x -> x.toFile().isDirectory())
                    .findFirst();
        }
    }

    private static Path defaultWindowsPath() {
        return dbPathRelativeToDriveRoot(Path.of(System.getenv("LOCALAPPDATA")));
    }

    private static Path defaultMacPath() {
        return dbPathRelativeToDriveRoot(
                Path.of(System.getProperty("user.home"), "Library", "Application Support")
        );
    }

    private static Path dbPathRelativeToDriveRoot(Path base) {
        return Path.of(base.toAbsolutePath().toString(), "Google", "Drive", "user_default", "sync_config.db");
    }

    private static Connection getConnectionToDb(Path dbPath) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath.toAbsolutePath().toString());
    }

    private static Path defaultPath() {
        return Path.of(System.getProperty("user.home"), "Google Drive");
    }

    private static Path reasonableRename() {
        return Path.of(System.getProperty("user.home"), "drive");
    }
}
