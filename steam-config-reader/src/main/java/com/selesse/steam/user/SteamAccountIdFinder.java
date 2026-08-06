package com.selesse.steam.user;

import com.selesse.steam.SteamAccountId;
import com.selesse.steam.registry.SteamRegistry;
import com.selesse.steam.registry.implementation.RegistryObject;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SteamAccountIdFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SteamAccountIdFinder.class);

    private final SteamRegistry registry;

    public SteamAccountIdFinder(SteamRegistry registry) {
        this.registry = registry;
    }

    public Optional<SteamAccountId> findCurrentUserId() {
        var loginUsersRegistryMaybe = readLoginUsers();
        if (loginUsersRegistryMaybe.isEmpty()) {
            LOGGER.info("Could not find loginusers.vdf");
            return Optional.empty();
        }
        var loginUsersRegistry = loginUsersRegistryMaybe.get();
        var userIds = loginUsersRegistry
                .findObject("users")
                .map(RegistryObject::getKeys)
                .orElse(List.of());

        if (userIds.isEmpty()) {
            return Optional.empty();
        }

        if (userIds.size() == 1) {
            return Optional.of(new SteamAccountId(userIds.get(0)));
        }

        var autoLoginUserId = userIds.stream()
                .filter(userId -> loginUsersRegistry
                        .findString("users/%s/AutoLogin".formatted(userId))
                        .map(autoLogin -> "1".equals(autoLogin.getValue()))
                        .orElse(false))
                .findFirst();

        if (autoLoginUserId.isPresent()) {
            return autoLoginUserId.map(SteamAccountId::new);
        }

        LOGGER.info("No user marked as AutoLogin, falling back to the most recently logged in user");
        return userIds.stream()
                .max(Comparator.comparingLong(userId -> getTimestamp(loginUsersRegistry, userId)))
                .map(SteamAccountId::new);
    }

    private static long getTimestamp(RegistryObject loginUsersRegistry, String userId) {
        return loginUsersRegistry
                .findString("users/%s/Timestamp".formatted(userId))
                .map(timestamp -> Long.parseLong(timestamp.getValue()))
                .orElse(0L);
    }

    @VisibleForTesting
    Optional<RegistryObject> readLoginUsers() {
        return registry.readLoginUsers();
    }
}
