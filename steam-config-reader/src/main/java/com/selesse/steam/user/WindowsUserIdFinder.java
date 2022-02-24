package com.selesse.steam.user;

import com.google.common.annotations.VisibleForTesting;
import com.selesse.processes.ProcessRunner;
import com.selesse.steam.SteamAccountId;
import com.selesse.steam.registry.windows.RegistryDwordParser;

import java.util.Optional;

public class WindowsUserIdFinder {
    public static Optional<SteamAccountId> find() {
        return new WindowsUserIdFinder().findCurrentActiveUser();
    }

    @VisibleForTesting
    WindowsUserIdFinder() {}

    @VisibleForTesting
    Optional<SteamAccountId> findCurrentActiveUser() {
        Optional<Long> activeUserMaybe = RegistryDwordParser.getValueFromOutput(getActiveProcessOutput(), "ActiveUser");
        return activeUserMaybe.map(SteamAccountId::from32Bit);
    }

    @VisibleForTesting
    String getActiveProcessOutput() {
        return new ProcessRunner("reg", "query", "HKEY_CURRENT_USER\\SOFTWARE\\Valve\\Steam\\ActiveProcess").runAndGetOutput();
    }
}
