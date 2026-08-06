package com.selesse.steam.crossplatform.sync.cloud;

import com.selesse.concurrent.IsolatedExecutors;
import com.selesse.files.RuntimeExceptionFiles;
import com.selesse.os.OperatingSystems;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.VisibleForTesting;

public class GoogleDriveProvider implements CloudStorageProvider {
    // Files.getFileStore() can block indefinitely (not just slowly) on a drive that's
    // inaccessible in the current session, e.g. a virtual cloud-storage drive when running as
    // a Windows service in Session 0. Timeouts below only stop the calling thread from waiting
    // on it; they can't interrupt the underlying blocked native call. Running these probes on
    // their own daemon-thread pool means a permanently stuck probe leaks a thread here instead
    // of on ForkJoinPool.commonPool(), where it would eventually starve unrelated work (like
    // hook execution) that also defaults to the common pool.
    @VisibleForTesting
    static final ExecutorService DRIVE_PROBE_EXECUTOR = IsolatedExecutors.newDaemonCachedPool("google-drive-probe");

    @Override
    public String getName() {
        return "google_drive";
    }

    @Override
    public Optional<Path> getRoot() {
        Optional<Path> googleDrive = findGoogleDriveBasedOnDrives();
        if (googleDrive.isPresent()) {
            return googleDrive;
        }

        Optional<Path> localDbPathMaybe =
                switch (OperatingSystems.get().family()) {
                    case MAC, LINUX -> defaultMacDriveConfigPath();
                    case WINDOWS -> defaultWindowsDriveConfigPath();
                };

        return localDbPathMaybe.map(this::loadGoogleDrivePathFromItsDatabase).orElseGet(this::defaultPathIfExists);
    }

    private Optional<Path> defaultPathIfExists() {
        return Stream.of(defaultPath(), defaultLegacyPath(), reasonableRename())
                .filter(x -> x.toFile().isDirectory())
                .findFirst();
    }

    private Optional<Path> findGoogleDriveBasedOnDrives() {
        // One shared deadline across all drives, not a fresh timeout per drive - otherwise this
        // could run for (number of drives) * LOOKUP_TIMEOUT, well past what the caller is
        // waiting for.
        Instant deadline = Instant.now().plus(LOOKUP_TIMEOUT);
        return StreamSupport.stream(
                        FileSystems.getDefault().getRootDirectories().spliterator(), false)
                .filter(drive -> isGoogleDrive(drive, deadline))
                .filter(drive ->
                        Path.of(drive.toString(), "My Drive.lnk").toFile().isFile())
                .map(x -> RuntimeExceptionFiles.resolveLnk(Path.of(x.toString(), "My Drive.lnk")))
                .findFirst();
    }

    private boolean isGoogleDrive(Path drive, Instant deadline) {
        Duration remaining = Duration.between(Instant.now(), deadline);
        if (remaining.isNegative() || remaining.isZero()) {
            return false;
        }
        try {
            return CompletableFuture.supplyAsync(
                            () -> RuntimeExceptionFiles.getFileStore(drive)
                                    .name()
                                    .equals("Google Drive"),
                            DRIVE_PROBE_EXECUTOR)
                    .get(remaining.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return false;
        }
    }

    private Optional<Path> loadGoogleDrivePathFromItsDatabase(Path localDbPath) {
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

    private Optional<Path> defaultWindowsDriveConfigPath() {
        return dbPathRelativeToDriveRoot(Path.of(System.getenv("LOCALAPPDATA")));
    }

    private Optional<Path> defaultMacDriveConfigPath() {
        return dbPathRelativeToDriveRoot(Path.of(System.getProperty("user.home"), "Library", "Application Support"));
    }

    private Optional<Path> dbPathRelativeToDriveRoot(Path base) {
        return Stream.of(newSyncConfigPath(base), legacySyncConfigPath(base))
                .filter(path -> path.toFile().isFile())
                .findFirst();
    }

    private Path legacySyncConfigPath(Path base) {
        return Path.of(base.toAbsolutePath().toString(), "Google", "Drive", "user_default", "sync_config.db");
    }

    private Path newSyncConfigPath(Path base) {
        return Path.of(
                base.toAbsolutePath().toString(),
                "Google",
                "DriveFS",
                "migration",
                "bns_config",
                "user_default",
                "sync_config.db");
    }

    private Connection getConnectionToDb(Path dbPath) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath.toAbsolutePath());
    }

    private Path defaultPath() {
        return Path.of(System.getProperty("user.home"), "Google Drive", "My Drive");
    }

    private Path defaultLegacyPath() {
        return Path.of(System.getProperty("user.home"), "Google Drive");
    }

    private Path reasonableRename() {
        return Path.of(System.getProperty("user.home"), "drive");
    }
}
