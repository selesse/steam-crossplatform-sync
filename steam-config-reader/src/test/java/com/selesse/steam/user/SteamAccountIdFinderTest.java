package com.selesse.steam.user;

import com.selesse.os.OperatingSystems;
import com.selesse.os.Resources;
import com.selesse.steam.SteamAccountId;
import org.junit.Assume;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class SteamAccountIdFinderTest {
    @Test
    public void canFindUserId_whenLoginUsersIsPresent() {
        Assume.assumeTrue(OperatingSystems.get() != OperatingSystems.OperatingSystem.WINDOWS);
        SteamAccountIdFinder userIdFinderSpy = Mockito.spy(new SteamAccountIdFinder());
        when(userIdFinderSpy.getLoginUsersPath(OperatingSystems.get()))
                .thenReturn(Optional.of(Resources.getResource("loginusers.vdf")));

        Optional<SteamAccountId> steamAccountIdMaybe = userIdFinderSpy.findMostRecentUserIdIfPresent();
        assertThat(steamAccountIdMaybe.orElseThrow())
                .isEqualTo(new SteamAccountId("76561197960287930"));
    }

    @Test
    public void doesNotFindUserId_whenLoginUsersIsNotPresent() {
        Assume.assumeTrue(OperatingSystems.get() != OperatingSystems.OperatingSystem.WINDOWS);
        var userIdFinderSpy = Mockito.spy(new SteamAccountIdFinder());
        when(userIdFinderSpy.getLoginUsersPath(OperatingSystems.get()))
                .thenReturn(Optional.empty());

        assertThat(userIdFinderSpy.findMostRecentUserIdIfPresent()).isEmpty();
    }
}