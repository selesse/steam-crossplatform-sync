package com.selesse.steam.steamcmd.games;

import com.selesse.steam.TestGames;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserFileSystemTest {
    @Test
    public void canPrintHollowKnightPaths() {
        TestGames hollowKnight = TestGames.HOLLOW_KNIGHT;
        UserFileSystem userFileSystem = hollowKnight.getUserFileSystem();

        assertThat(userFileSystem.getWindowsInstallationPath())
                .isEqualTo("%USERPROFILE%/AppData/LocalLow/Team Cherry/Hollow Knight");
        assertThat(userFileSystem.getMacInstallationPath())
                .isEqualTo("~/Library/Application Support/unity.Team Cherry.Hollow Knight");
    }

    @Test
    public void canPrintWargroove() {
        TestGames warGroove = TestGames.WARGROOVE;
        UserFileSystem userFileSystem = warGroove.getUserFileSystem();

        assertThat(userFileSystem.getWindowsInstallationPath())
                .isEqualTo("%USERPROFILE%/AppData/Roaming/Chucklefish/Wargroove/save");
        assertThat(userFileSystem.getMacInstallationPath())
                .isEqualTo("~/Library/Chucklefish/Wargroove/save");
    }

    @Test
    public void canPrintSlayTheSpire() {
        TestGames slayTheSpire = TestGames.SLAY_THE_SPIRE;
        UserFileSystem userFileSystem = slayTheSpire.getUserFileSystem();

        assertThat(userFileSystem.getWindowsInstallationPath())
                .isEqualTo("%PROGRAMFILES(X86)%/Steam/steamsapps/common/SlayTheSpire/saves");
        assertThat(userFileSystem.getMacInstallationPath())
                .isEqualTo("~/Library/Application Support/Steam/steamapps/" +
                        "common/SlayTheSpire/SlayTheSpire.app/Contents/Resources/saves");
    }

    @Test
    public void canPrintOxygenNotIncluded() {
        TestGames oxygenNotIncluded = TestGames.OXYGEN_NOT_INCLUDED;
        UserFileSystem userFileSystem = oxygenNotIncluded.getUserFileSystem();

        assertThat(userFileSystem.getWindowsInstallationPath())
                .isEqualTo("%USERPROFILE%/Documents/Klei/OxygenNotIncluded/save_files");
        assertThat(userFileSystem.getMacInstallationPath())
                .isEqualTo("~/Library/Application Support/unity.Klei.Oxygen Not Included/save_files");
    }
}