package com.selesse.steam;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Assert;
import org.junit.Test;

public class SteamAccountIdTest {
    @Test
    public void canProperlyCompute_32BitIds() {
        var accountId = new SteamAccountId("76561197960287930");
        assertThat(accountId.to32BitId()).isEqualTo(22202);
    }

    @Test
    public void canProperlyCompute_publicValue() {
        var accountId = new SteamAccountId("76561197960287930");
        assertThat(accountId.isPublic()).isEqualTo(false);
    }

    @Test
    public void disallowsInvalidIds_inTheConstructor() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new SteamAccountId("lol"));
    }

    @Test
    public void recognizesSixtyFourBitIds() {
        assertThat(SteamAccountId.isSixtyFourBitId("76561198009129143")).isTrue();
        assertThat(SteamAccountId.isSixtyFourBitId("76561199039324428")).isTrue();
    }

    @Test
    public void doesNotMistakeOtherFolderNamesForSixtyFourBitIds() {
        assertThat(SteamAccountId.isSixtyFourBitId("48863415")).isFalse(); // 32-bit form
        assertThat(SteamAccountId.isSixtyFourBitId("User_76561198009129143")).isFalse();
        assertThat(SteamAccountId.isSixtyFourBitId("76561197960265728")).isFalse(); // account 0
        assertThat(SteamAccountId.isSixtyFourBitId("99999999999999999999")).isFalse();
        assertThat(SteamAccountId.isSixtyFourBitId("saves")).isFalse();
        assertThat(SteamAccountId.isSixtyFourBitId("")).isFalse();
    }
}
