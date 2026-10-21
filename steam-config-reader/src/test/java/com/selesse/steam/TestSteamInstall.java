package com.selesse.steam;

import com.selesse.steam.registry.SteamRegistry;

/**
 * The {@link SteamInstall} tests read through: a real registry, but a fixed account.
 *
 * <p>Pinning the account keeps expected and actual paths substituting the same value on any
 * machine, signed in or not. The environment is empty for the same reason: paths that depend on
 * {@code XDG_DATA_HOME} and friends resolve to their spec defaults rather than to the developer's setup.
 */
public final class TestSteamInstall {
    public static final SteamAccountId ACCOUNT_ID = new SteamAccountId("76561197960287930");

    public static SteamInstall get() {
        return new SteamInstall(new SteamRegistry(), ACCOUNT_ID, name -> null);
    }

    private TestSteamInstall() {}
}
