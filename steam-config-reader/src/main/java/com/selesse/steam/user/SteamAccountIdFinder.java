package com.selesse.steam.user;

import com.google.common.annotations.VisibleForTesting;
import com.selesse.steam.SteamAccountId;
import com.selesse.steam.registry.SteamRegistry;
import com.selesse.steam.registry.implementation.RegistryObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
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
        var userIds = Optional.ofNullable(loginUsersRegistry.getObjectValueAsObject("users"))
                .map(RegistryObject::getKeys)
                .orElse(new ArrayList<>());

        if (userIds.isEmpty()) {
            return Optional.empty();
        }

        if (userIds.size() == 1) {
            return Optional.of(new SteamAccountId(userIds.get(0)));
        }

        var autoLoginUserId = userIds.stream()
                .filter(userId -> {
                    var autoLogin = loginUsersRegistry.getObjectValueAsString("users/%s/AutoLogin".formatted(userId));
                    return autoLogin != null && "1".equals(autoLogin.getValue());
                })
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
        var timestamp = loginUsersRegistry.getObjectValueAsString("users/%s/Timestamp".formatted(userId));
        return timestamp != null ? Long.parseLong(timestamp.getValue()) : 0L;
    }

    @VisibleForTesting
    Optional<RegistryObject> readLoginUsers() {
        return registry.readLoginUsers();
    }
}
