package com.selesse.steam.steamcmd.games;

import com.selesse.steam.TestGames;
import com.selesse.steam.registry.implementation.RegistryStore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserFileSystemTest {
    @Test
    public void canPrintHollowKnightPaths() {
        RegistryStore ufs = TestGames.HOLLOW_KNIGHT.getUserFileSystemStore();
        UserFileSystem userFileSystem = new UserFileSystem(ufs);

        assertThat(userFileSystem.getWindowsInstallationPath())
                .isEqualTo("%USERPROFILE%/AppData/LocalLow/Team Cherry/Hollow Knight");
        assertThat(userFileSystem.getMacInstallationPath())
                .isEqualTo("~/Library/Application Support/unity.Team Cherry.Hollow Knight");
    }

    @Test
    public void canPrintWargroove() {
        RegistryStore ufs = TestGames.WARGROOVE.getUserFileSystemStore();
        UserFileSystem userFileSystem = new UserFileSystem(ufs);

        assertThat(userFileSystem.getWindowsInstallationPath())
                .isEqualTo("%USERPROFILE%/AppData/Roaming/Chucklefish/Wargroove/save");
        assertThat(userFileSystem.getMacInstallationPath())
                .isEqualTo("~/Library/Chucklefish/Wargroove/save");
    }
}