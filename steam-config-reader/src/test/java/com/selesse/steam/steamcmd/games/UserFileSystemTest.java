package com.selesse.steam.steamcmd.games;

import com.selesse.steam.TestGames;
import com.selesse.steam.games.UserFileSystem;
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
        assertThat(userFileSystem.getLinuxInstallationPath())
                .isEqualTo("~/.config/unity3d/Team Cherry/Hollow Knight");
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
                .isEqualTo("%PROGRAMFILES(X86)%/Steam/steamapps/common/SlayTheSpire/saves");
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

    @Test
    public void canPrintLegendOfGrimrock() {
        TestGames oxygenNotIncluded = TestGames.LEGEND_OF_GRIMROCK;
        UserFileSystem userFileSystem = oxygenNotIncluded.getUserFileSystem();

        assertThat(userFileSystem.getWindowsInstallationPath())
                .isEqualTo("%USERPROFILE%/Documents/Almost Human/Legend of Grimrock");
        assertThat(userFileSystem.getMacInstallationPath())
                .isEqualTo("~/Library/Application Support/Almost Human/Legend of Grimrock");
    }

    @Test
    public void canPrintTorchlightII() {
        TestGames torchlight2 = TestGames.TORCHLIGHT_II;
        UserFileSystem userFileSystem = torchlight2.getUserFileSystem();

        assertThat(userFileSystem.getWindowsInstallationPath())
                .isEqualTo("%USERPROFILE%/Documents/my games/runic games/torchlight 2/save");
        assertThat(userFileSystem.getMacInstallationPath())
                .isEqualTo("~/Library/Application Support/Steam/steamapps/common/" +
                        "Torchlight II/my games/runic games/torchlight 2/save");
        assertThat(userFileSystem.getLinuxInstallationPath())
                .isEqualTo("~/.steam/steamapps/common/" +
                        "Torchlight II/my games/runic games/torchlight 2/save");
    }

    @Test
    public void canPrintUnrailed() {
        TestGames unrailed = TestGames.UNRAILED;
        UserFileSystem userFileSystem = unrailed.getUserFileSystem();

        assertThat(userFileSystem.getWindowsInstallationPath())
                .isEqualTo("%USERPROFILE%/AppData/Local/Daedalic Entertainment GmbH/Unrailed/GameState/AllPlayers/SaveGames");
        assertThat(userFileSystem.getMacInstallationPath())
                .isEqualTo("~/Library/Application Support/UnrailedGame/GameState/AllPlayers/SaveGames");
        assertThat(userFileSystem.getLinuxInstallationPath())
                .isEqualTo("~/.local/share/UnrailedGame/GameState/AllPlayers/SaveGames");
    }

    @Test
    public void canPrintOriAndTheWillOfTheWisps() {
        TestGames ori = TestGames.ORI_AND_THE_WILL_OF_THE_WISPS;
        UserFileSystem userFileSystem = ori.getUserFileSystem();

        assertThat(userFileSystem.getWindowsInstallationPath())
                .isEqualTo("%USERPROFILE%/AppData/Local/Ori and the Will of The Wisps");
    }

    @Test
    public void canPrintInscryption() {
        TestGames inscryption = TestGames.INSCRYPTION;
        UserFileSystem userFileSystem = inscryption.getUserFileSystem();

        assertThat(userFileSystem.getWindowsInstallationPath())
                .isEqualTo("%PROGRAMFILES(X86)%/Steam/steamapps/common/Inscryption/");
    }
}