package com.selesse.steam.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.selesse.files.RuntimeExceptionFiles;
import com.selesse.os.Resources;
import com.selesse.steam.SteamAccountId;
import com.selesse.steam.registry.SteamRegistry;
import com.selesse.steam.registry.implementation.RegistryParser;
import java.util.Optional;
import java.util.OptionalLong;
import org.junit.Test;
import org.mockito.Mockito;

public class SteamAccountIdFinderTest {
    @Test
    public void findsAutoLoginUser_whenMultipleUsersArePresent() {
        SteamAccountIdFinder userIdFinderSpy = Mockito.spy(new SteamAccountIdFinder(new SteamRegistry()));
        var loginUsers =
                RegistryParser.parse(RuntimeExceptionFiles.readAllLines(Resources.getResource("loginusers.vdf")));
        doReturn(Optional.of(loginUsers)).when(userIdFinderSpy).readLoginUsers();

        Optional<SteamAccountId> steamAccountIdMaybe = userIdFinderSpy.findCurrentUserId();
        assertThat(steamAccountIdMaybe.orElseThrow()).isEqualTo(new SteamAccountId("76561197960287930"));
    }

    @Test
    public void doesNotFindUserId_whenLoginUsersIsNotPresent() {
        var userIdFinderSpy = Mockito.spy(new SteamAccountIdFinder(new SteamRegistry()));
        doReturn(Optional.empty()).when(userIdFinderSpy).readLoginUsers();

        assertThat(userIdFinderSpy.findCurrentUserId()).isEmpty();
    }

    @Test
    public void fallsBackToOnlyUserId_whenOnlyOneUserIsPresent() {
        SteamAccountIdFinder userIdFinderSpy = Mockito.spy(new SteamAccountIdFinder(new SteamRegistry()));
        var loginUsers = RegistryParser.parse(
                RuntimeExceptionFiles.readAllLines(Resources.getResource("loginusers_single.vdf")));
        doReturn(Optional.of(loginUsers)).when(userIdFinderSpy).readLoginUsers();

        Optional<SteamAccountId> steamAccountIdMaybe = userIdFinderSpy.findCurrentUserId();
        assertThat(steamAccountIdMaybe.orElseThrow()).isEqualTo(new SteamAccountId("76561198009129143"));
    }

    @Test
    public void fallsBackToMostRecentTimestamp_whenNoUserIsMarkedAutoLogin() {
        SteamAccountIdFinder userIdFinderSpy = Mockito.spy(new SteamAccountIdFinder(new SteamRegistry()));
        var loginUsers = RegistryParser.parse(
                RuntimeExceptionFiles.readAllLines(Resources.getResource("loginusers_timestamp_fallback.vdf")));
        doReturn(Optional.of(loginUsers)).when(userIdFinderSpy).readLoginUsers();

        Optional<SteamAccountId> steamAccountIdMaybe = userIdFinderSpy.findCurrentUserId();
        assertThat(steamAccountIdMaybe.orElseThrow()).isEqualTo(new SteamAccountId("76561199027325428"));
    }

    // 1067059700 is the 32-bit form of 76561199027325428, bobloblaw in loginusers.vdf - who isn't
    // the AutoLogin user, so only ActiveUser can pick them.
    @Test
    public void findSignedInUserIdUsesSteamsActiveUser() {
        SteamAccountIdFinder userIdFinderSpy = spyWithLoginUsers("loginusers.vdf");
        doReturn(OptionalLong.of(1067059700L)).when(userIdFinderSpy).readActiveUser();

        assertThat(userIdFinderSpy.findSignedInUserId()).contains(new SteamAccountId("76561199027325428"));
    }

    @Test
    public void findSignedInUserIdFallsBackToLoginUsers_whenNobodyIsSignedIn() {
        SteamAccountIdFinder userIdFinderSpy = spyWithLoginUsers("loginusers.vdf");
        doReturn(OptionalLong.of(0L)).when(userIdFinderSpy).readActiveUser();

        assertThat(userIdFinderSpy.findSignedInUserId()).contains(new SteamAccountId("76561197960287930"));
    }

    @Test
    public void findSignedInUserIdFallsBackToLoginUsers_whenSteamDoesNotSay() {
        SteamAccountIdFinder userIdFinderSpy = spyWithLoginUsers("loginusers.vdf");
        doReturn(OptionalLong.empty()).when(userIdFinderSpy).readActiveUser();

        assertThat(userIdFinderSpy.findSignedInUserId()).contains(new SteamAccountId("76561197960287930"));
    }

    private SteamAccountIdFinder spyWithLoginUsers(String resource) {
        SteamAccountIdFinder userIdFinderSpy = Mockito.spy(new SteamAccountIdFinder(new SteamRegistry()));
        var loginUsers = RegistryParser.parse(RuntimeExceptionFiles.readAllLines(Resources.getResource(resource)));
        doReturn(Optional.of(loginUsers)).when(userIdFinderSpy).readLoginUsers();
        return userIdFinderSpy;
    }
}
