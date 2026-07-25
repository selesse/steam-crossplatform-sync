package com.selesse.steam.user;

import com.google.common.annotations.VisibleForTesting;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.SteamAccountId;
import com.selesse.steam.registry.SteamRegistry;
import com.selesse.steam.registry.implementation.RegistryObject;
import java.util.ArrayList;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SteamAccountIdFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SteamAccountIdFinder.class);

    @VisibleForTesting
    SteamAccountIdFinder() {}

    private Optional<SteamAccountId> find() {
        var os = OperatingSystems.get();
        if (os == OperatingSystems.OperatingSystem.WINDOWS) {
            return WindowsUserIdFinder.find();
        } else {
            return new SteamAccountIdFinder().findMostRecentUserIdIfPresent();
        }
    }

    public static Optional<SteamAccountId> findIfPresent() {
        return new SteamAccountIdFinder().find();
    }

    Optional<SteamAccountId> findMostRecentUserIdIfPresent() {
        var loginUsersRegistryMaybe = readLoginUsers();
        if (loginUsersRegistryMaybe.isEmpty()) {
            LOGGER.info("Could not find loginusers.vdf");
            return Optional.empty();
        }
        var loginUsersRegistry = loginUsersRegistryMaybe.get();
        var userIds = Optional.ofNullable(loginUsersRegistry.getObjectValueAsObject("users"))
                .map(RegistryObject::getKeys)
                .orElse(new ArrayList<>());

        var mostRecentUserId = userIds.stream()
                .filter(userId -> {
                    var mostRecent = loginUsersRegistry.getObjectValueAsString("users/%s/MostRecent".formatted(userId));
                    return mostRecent != null && "1".equals(mostRecent.getValue());
                })
                .findFirst();

        if (mostRecentUserId.isPresent()) {
            return mostRecentUserId.map(SteamAccountId::new);
        }

        if (userIds.size() == 1) {
            LOGGER.info("No user marked as MostRecent, falling back to the only user present");
            return Optional.of(new SteamAccountId(userIds.get(0)));
        }

        return Optional.empty();
    }

    @VisibleForTesting
    Optional<RegistryObject> readLoginUsers() {
        return SteamRegistry.getInstance().readLoginUsers();
    }
}
