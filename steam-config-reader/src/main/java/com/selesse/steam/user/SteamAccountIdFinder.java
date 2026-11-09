package com.selesse.steam.user;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.SteamAccountId;
import com.selesse.steam.registry.SteamRegistry;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.windows.WindowsActiveUser;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SteamAccountIdFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SteamAccountIdFinder.class);

    private final SteamRegistry registry;

    public SteamAccountIdFinder(SteamRegistry registry) {
        this.registry = registry;
    }

    /**
     * Whoever is signed in to Steam right now, falling back to {@link #findCurrentUserId()}'s
     * best guess when Steam doesn't say: nobody is signed in, or this isn't Windows.
     *
     * <p>Unlike {@link #findCurrentUserId()} this tells apart several accounts sharing one machine,
     * so it's worth asking again whenever the answer matters rather than once at startup.
     */
    public Optional<SteamAccountId> findSignedInUserId() {
        OptionalLong activeUser = readActiveUser();
        if (activeUser.isPresent() && activeUser.getAsLong() != 0) {
            return Optional.of(SteamAccountId.from32Bit(activeUser.getAsLong()));
        }
        return findCurrentUserId();
    }

    /**
     * The best guess at the account in use, from who has signed in on this machine before: the only
     * one, the one set to sign in automatically, or else the most recent.
     */
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

    /**
     * The 32-bit account id Steam says is signed in, which is 0 when nobody is. Only Windows records
     * this: Steam on Mac and SteamOS writes no {@code ActiveProcess} block to {@code registry.vdf}.
     */
    @VisibleForTesting
    OptionalLong readActiveUser() {
        if (OperatingSystems.get().family() == OperatingSystems.OperatingSystemFamily.WINDOWS) {
            return WindowsActiveUser.get();
        }
        return OptionalLong.empty();
    }

    @VisibleForTesting
    Optional<RegistryObject> readLoginUsers() {
        return registry.readLoginUsers();
    }
}
