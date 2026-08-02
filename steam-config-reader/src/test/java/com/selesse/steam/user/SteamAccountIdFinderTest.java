package com.selesse.steam.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.selesse.files.RuntimeExceptionFiles;
import com.selesse.os.Resources;
import com.selesse.steam.SteamAccountId;
import com.selesse.steam.registry.implementation.RegistryParser;
import java.util.Optional;
import org.junit.Test;
import org.mockito.Mockito;

public class SteamAccountIdFinderTest {
    @Test
    public void findsAutoLoginUser_whenMultipleUsersArePresent() {
        SteamAccountIdFinder userIdFinderSpy = Mockito.spy(new SteamAccountIdFinder());
        var loginUsers =
                RegistryParser.parse(RuntimeExceptionFiles.readAllLines(Resources.getResource("loginusers.vdf")));
        doReturn(Optional.of(loginUsers)).when(userIdFinderSpy).readLoginUsers();

        Optional<SteamAccountId> steamAccountIdMaybe = userIdFinderSpy.findCurrentUserId();
        assertThat(steamAccountIdMaybe.orElseThrow()).isEqualTo(new SteamAccountId("76561197960287930"));
    }

    @Test
    public void doesNotFindUserId_whenLoginUsersIsNotPresent() {
        var userIdFinderSpy = Mockito.spy(new SteamAccountIdFinder());
        doReturn(Optional.empty()).when(userIdFinderSpy).readLoginUsers();

        assertThat(userIdFinderSpy.findCurrentUserId()).isEmpty();
    }

    @Test
    public void fallsBackToOnlyUserId_whenOnlyOneUserIsPresent() {
        SteamAccountIdFinder userIdFinderSpy = Mockito.spy(new SteamAccountIdFinder());
        var loginUsers = RegistryParser.parse(
                RuntimeExceptionFiles.readAllLines(Resources.getResource("loginusers_single.vdf")));
        doReturn(Optional.of(loginUsers)).when(userIdFinderSpy).readLoginUsers();

        Optional<SteamAccountId> steamAccountIdMaybe = userIdFinderSpy.findCurrentUserId();
        assertThat(steamAccountIdMaybe.orElseThrow()).isEqualTo(new SteamAccountId("76561198009129143"));
    }

    @Test
    public void fallsBackToMostRecentTimestamp_whenNoUserIsMarkedAutoLogin() {
        SteamAccountIdFinder userIdFinderSpy = Mockito.spy(new SteamAccountIdFinder());
        var loginUsers = RegistryParser.parse(
                RuntimeExceptionFiles.readAllLines(Resources.getResource("loginusers_timestamp_fallback.vdf")));
        doReturn(Optional.of(loginUsers)).when(userIdFinderSpy).readLoginUsers();

        Optional<SteamAccountId> steamAccountIdMaybe = userIdFinderSpy.findCurrentUserId();
        assertThat(steamAccountIdMaybe.orElseThrow()).isEqualTo(new SteamAccountId("76561199027325428"));
    }
}
