package com.selesse.steam.user;

import com.google.common.base.Joiner;
import com.selesse.steam.SteamAccountId;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class WindowsUserIdFinderTest {
    @Test
    public void testCanFindSteamAccountId() {
        var windowsUserIdFinder = Mockito.spy(new WindowsUserIdFinder());

        List<String> sampleOutput = List.of(
                "",
                "HKEY_CURRENT_USER\\SOFTWARE\\Valve\\Steam\\ActiveProcess",
                "pid    REG_DWORD    0x3708",
                "SteamClientDll    REG_SZ    C:\\Program Files (x86)\\Steam\\steamclient.dll",
                "SteamClientDll64    REG_SZ    C:\\Program Files (x86)\\Steam\\steamclient64.dll",
                "Universe    REG_SZ    Public",
                "ActiveUser    REG_DWORD    0x56BA",
                ""
        );

        when(windowsUserIdFinder.getActiveProcessOutput()).thenReturn(Joiner.on("\n").join(sampleOutput));


        assertThat(windowsUserIdFinder.findCurrentActiveUser())
                .isPresent()
                .hasValue(new SteamAccountId("76561197960287930"));
    }

}