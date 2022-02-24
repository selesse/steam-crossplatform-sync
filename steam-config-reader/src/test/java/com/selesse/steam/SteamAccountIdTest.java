package com.selesse.steam;

import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}