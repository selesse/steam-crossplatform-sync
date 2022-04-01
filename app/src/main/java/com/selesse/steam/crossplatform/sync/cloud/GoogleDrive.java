package com.selesse.steam.crossplatform.sync.cloud;

import com.google.common.collect.Lists;
import com.selesse.files.RuntimeExceptionFiles;
import com.selesse.os.OperatingSystems;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.*;
import java.util.Optional;
import java.util.stream.Stream;

public class GoogleDrive {
    public static Optional<Path> getDriveRoot() {
        Optional<Path> googleDrive = findGoogleDriveBasedOnDrives();
        if (googleDrive.isPresent()) {
            return googleDrive;
        }

        Optional<Path> localDbPathMaybe =
                switch (OperatingSystems.get()) {
                    case MAC, LINUX -> defaultMacDriveConfigPath();
                    case WINDOWS -> defaultWindowsDriveConfigPath();
                };

        return localDbPathMaybe
                .map(GoogleDrive::loadGoogleDrivePathFromItsDatabase)
                .orElse(defaultPathIfExists());
    }

    private static Optional<Path> defaultPathIfExists() {
        return Stream.of(defaultPath(), defaultLegacyPath(), reasonableRename())
                .filter(x -> x.toFile().isDirectory())
                .findFirst();
    }

    private static Optional<Path> findGoogleDriveBasedOnDrives() {
        return Lists.newArrayList(FileSystems.getDefault().getRootDirectories()).stream()
                .filter(drive ->
                        RuntimeExceptionFiles.getFileStore(drive).name().equals("Google Drive"))
                .filter(drive ->
                        Path.of(drive.toString(), "My Drive.lnk").toFile().isFile())
                .map(x -> RuntimeExceptionFiles.resolveLnk(Path.of(x.toString(), "My Drive.lnk")))
                .findFirst();
    }

    private static Optional<Path> loadGoogleDrivePathFromItsDatabase(Path localDbPath) {
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
            return defaultPathIfExists();
        }
    }

    private static Optional<Path> defaultWindowsDriveConfigPath() {
        return dbPathRelativeToDriveRoot(Path.of(System.getenv("LOCALAPPDATA")));
    }

    private static Optional<Path> defaultMacDriveConfigPath() {
        return dbPathRelativeToDriveRoot(Path.of(System.getProperty("user.home"), "Library", "Application Support"));
    }

    private static Optional<Path> dbPathRelativeToDriveRoot(Path base) {
        return Stream.of(newSyncConfigPath(base), legacySyncConfigPath(base))
                .filter(path -> path.toFile().isFile())
                .findFirst();
    }

    private static Path legacySyncConfigPath(Path base) {
        return Path.of(base.toAbsolutePath().toString(), "Google", "Drive", "user_default", "sync_config.db");
    }

    private static Path newSyncConfigPath(Path base) {
        return Path.of(
                base.toAbsolutePath().toString(),
                "Google",
                "DriveFS",
                "migration",
                "bns_config",
                "user_default",
                "sync_config.db");
    }

    private static Connection getConnectionToDb(Path dbPath) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath.toAbsolutePath());
    }

    private static Path defaultPath() {
        return Path.of(System.getProperty("user.home"), "Google Drive", "My Drive");
    }

    private static Path defaultLegacyPath() {
        return Path.of(System.getProperty("user.home"), "Google Drive");
    }

    private static Path reasonableRename() {
        return Path.of(System.getProperty("user.home"), "drive");
    }
}
