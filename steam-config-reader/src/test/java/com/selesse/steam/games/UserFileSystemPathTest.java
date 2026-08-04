package com.selesse.steam.games;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.steam.TestSteamInstall;
import org.junit.Test;

public class UserFileSystemPathTest {
    @Test
    public void testCanHandleTorchlight() {
        UserFileSystemPath userFileSystemPath = new UserFileSystemPath(
                "~/Library/Application Support/Steam/steamapps/common/Torchlight II",
                "my games/runic games/torchlight 2/save/{64BitSteamID}/",
                TestSteamInstall.ACCOUNT_ID);

        assertThat(userFileSystemPath.getSymbolPath())
                .isEqualTo(
                        "~/Library/Application Support/Steam/steamapps/common/Torchlight II/my games/runic games/torchlight 2/save/%s/"
                                .formatted(TestSteamInstall.ACCOUNT_ID.to64Bit()));
    }

    @Test
    public void rerootForProtonReplacesUserProfileWithTheProtonPrefix() {
        UserFileSystemPath userFileSystemPath = new UserFileSystemPath(
                "WinAppDataLocalLow", "Team Cherry/Hollow Knight", TestSteamInstall.ACCOUNT_ID, "*.dat", false, null);

        UserFileSystemPath rerooted = userFileSystemPath.rerootForProton(
                "~/.steam/steam/steamapps/compatdata/367520/pfx/drive_c/users/steamuser");

        assertThat(rerooted.getSymbolPath())
                .isEqualTo(
                        "~/.steam/steam/steamapps/compatdata/367520/pfx/drive_c/users/steamuser/AppData/LocalLow/Team Cherry/Hollow Knight/*.dat");
    }
}
