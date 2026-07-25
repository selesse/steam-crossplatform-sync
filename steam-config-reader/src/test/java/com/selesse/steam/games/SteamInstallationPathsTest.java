package com.selesse.steam.games;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.os.OperatingSystems;
import org.junit.Test;

public class SteamInstallationPathsTest {
    @Test
    public void linuxAndSteamOsShareTheSameRoot() {
        assertThat(SteamInstallationPaths.getRoot(OperatingSystems.OperatingSystem.LINUX))
                .isEqualTo(SteamInstallationPaths.getRoot(OperatingSystems.OperatingSystem.STEAM_OS))
                .isEqualTo("~/.steam/steam");
    }

    @Test
    public void getProtonPrefixUserProfileRootBuildsTheExpectedPath() {
        assertThat(SteamInstallationPaths.getProtonPrefixUserProfileRoot(646570L))
                .isEqualTo("~/.steam/steam/steamapps/compatdata/646570/pfx/drive_c/users/steamuser");
    }
}
