package com.selesse.steam;

import com.selesse.steam.registry.SteamRegistry;
import com.selesse.steam.user.SteamAccountIdFinder;
import java.util.function.Function;

/**
 * The Steam installation everything else reads through: where its files are, and who is signed in.
 *
 * <p>Both are properties of the machine rather than of any one app, and resolving the account
 * involves parsing {@code loginusers.vdf}, so they're detected once and passed down instead of
 * being looked up wherever they happen to be needed.
 *
 * @param registry reader for Steam's on-disk VDF files
 * @param accountId the signed-in account, or {@code null} if none could be determined
 * @param env looks up an environment variable, returning {@code null} when it's unset
 */
public record SteamInstall(SteamRegistry registry, SteamAccountId accountId, Function<String, String> env) {
    public SteamInstall(SteamRegistry registry, SteamAccountId accountId) {
        this(registry, accountId, System::getenv);
    }

    public static SteamInstall detect() {
        SteamRegistry registry = new SteamRegistry();
        return new SteamInstall(
                registry, new SteamAccountIdFinder(registry).findCurrentUserId().orElse(null));
    }
}
