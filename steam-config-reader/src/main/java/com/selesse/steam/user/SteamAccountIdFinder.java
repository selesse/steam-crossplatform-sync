package com.selesse.steam.user;

import com.selesse.files.RuntimeExceptionFiles;
import com.selesse.os.FilePathSanitizer;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.SteamAccountId;
import com.selesse.steam.games.SteamInstallationPaths;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;

public class SteamAccountIdFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SteamAccountIdFinder.class);

    SteamAccountIdFinder() {}

    public static Optional<SteamAccountId> findIfPresent() {
        return new SteamAccountIdFinder().findMostRecentUserIdIfPresent();
    }

    Optional<SteamAccountId> findMostRecentUserIdIfPresent() {
        var os = OperatingSystems.get();
        if (os == OperatingSystems.OperatingSystem.WINDOWS) {
            return WindowsUserIdFinder.find();
        }
        var loginUsersFile = getLoginUsersPath(os);
        if (loginUsersFile.isEmpty()) {
            LOGGER.info("Could not find loginusers.vdf");
            return Optional.empty();
        }
        var loginUsersRegistry = RegistryParser.parse(RuntimeExceptionFiles.readAllLines(loginUsersFile.get()));
        var userIds = Optional.ofNullable(loginUsersRegistry.getObjectValueAsObject("users"))
                .map(RegistryObject::getKeys).orElse(new ArrayList<>());

        return userIds.stream()
                .filter(userId -> loginUsersRegistry.getObjectValueAsString("users/%s/MostRecent".formatted(userId)).getValue().equals("1"))
                .map(SteamAccountId::new)
                .findFirst();
    }

    Optional<Path> getLoginUsersPath(OperatingSystems.OperatingSystem os) {
        String steamRoot = FilePathSanitizer.sanitize(SteamInstallationPaths.getRoot(os));
        Path loginUsersPath = Path.of(steamRoot, "config/loginusers.vdf").toAbsolutePath();
        if (loginUsersPath.toFile().exists()) {
            return Optional.of(loginUsersPath);
        }
        return Optional.empty();
    }
}
