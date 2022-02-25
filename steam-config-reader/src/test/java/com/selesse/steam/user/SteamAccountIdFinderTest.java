package com.selesse.steam.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.selesse.os.OperatingSystems;
import com.selesse.os.Resources;
import com.selesse.steam.SteamAccountId;
import java.util.Optional;
import org.junit.Test;
import org.mockito.Mockito;

public class SteamAccountIdFinderTest {
    @Test
    public void canFindUserId_whenLoginUsersIsPresent() {
        SteamAccountIdFinder userIdFinderSpy = Mockito.spy(new SteamAccountIdFinder());
        doReturn(Optional.of(Resources.getResource("loginusers.vdf")))
                .when(userIdFinderSpy)
                .getLoginUsersPath(OperatingSystems.get());

        Optional<SteamAccountId> steamAccountIdMaybe = userIdFinderSpy.findMostRecentUserIdIfPresent();
        assertThat(steamAccountIdMaybe.orElseThrow()).isEqualTo(new SteamAccountId("76561197960287930"));
    }

    @Test
    public void doesNotFindUserId_whenLoginUsersIsNotPresent() {
        var userIdFinderSpy = Mockito.spy(new SteamAccountIdFinder());
        doReturn(Optional.empty()).when(userIdFinderSpy).getLoginUsersPath(OperatingSystems.get());

        assertThat(userIdFinderSpy.findMostRecentUserIdIfPresent()).isEmpty();
    }
}
