package com.selesse.steam.registry.windows;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class WindowsActiveUserTest {
    // Values from a real machine, before and after switching Steam accounts: 0x2e998b7 and
    // 0x4051210c are 76561198009129143 and 76561199039324428 in 32-bit form.
    // This first one is reg's output byte for byte, CRLF line endings included.
    @Test
    public void parsesTheDwordFromRegQueryOutput() {
        String output = "\r\nHKEY_CURRENT_USER\\Software\\Valve\\Steam\\ActiveProcess\r\n"
                + "    ActiveUser    REG_DWORD    0x2e998b7\r\n\r\n";

        assertThat(WindowsActiveUser.parse(output)).hasValue(48863415L);
    }

    // Above 2^31, so this would overflow if it were parsed as a signed int.
    @Test
    public void parsesAnAccountIdAboveTheSignedIntRange() {
        String output = """

                HKEY_CURRENT_USER\\Software\\Valve\\Steam\\ActiveProcess
                    ActiveUser    REG_DWORD    0x4051210c

                """;

        assertThat(WindowsActiveUser.parse(output)).hasValue(1079058700L);
    }

    @Test
    public void parsesZeroWhenNobodyIsSignedIn() {
        String output = """

                HKEY_CURRENT_USER\\Software\\Valve\\Steam\\ActiveProcess
                    ActiveUser    REG_DWORD    0x0

                """;

        assertThat(WindowsActiveUser.parse(output)).hasValue(0L);
    }

    // reg prints its "unable to find" error to stderr, so a missing key leaves stdout empty.
    @Test
    public void isEmptyWhenTheKeyIsMissing() {
        assertThat(WindowsActiveUser.parse("")).isEmpty();
    }
}
