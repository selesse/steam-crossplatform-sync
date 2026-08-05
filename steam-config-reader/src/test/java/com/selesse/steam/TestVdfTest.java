package com.selesse.steam;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.steam.registry.implementation.RegistryObject;
import java.util.List;
import org.junit.Test;

// TestVdf builds most of the VDF fixtures in this module, so a quiet bug here would hand every
// other test a structure that parses but isn't the one it meant to write.
public class TestVdfTest {
    @Test
    public void indentsWithTabsAndSeparatesKeyFromValueWithOne() {
        List<String> lines = TestVdf.lines("""
                "common"
                {
                  "name" "Test Game"
                }
                """);

        assertThat(lines).containsExactly("\"common\"", "{", "\t\"name\"\t\"Test Game\"", "}");
    }

    @Test
    public void nestsOneTabPerTwoSpaces() {
        List<String> lines = TestVdf.lines("""
                "depots"
                {
                  "11"
                  {
                    "config"
                    {
                      "oslist" "windows"
                    }
                  }
                }
                """);

        assertThat(lines)
                .containsExactly(
                        "\"depots\"",
                        "{",
                        "\t\"11\"",
                        "\t{",
                        "\t\t\"config\"",
                        "\t\t{",
                        "\t\t\t\"oslist\"\t\"windows\"",
                        "\t\t}",
                        "\t}",
                        "}");
    }

    @Test
    public void leavesSpacesInsideAValueAlone() {
        List<String> lines = TestVdf.lines("""
                "path" "TestCo/Test Game/Saves"
                """);

        assertThat(lines).containsExactly("\"path\"\t\"TestCo/Test Game/Saves\"");
    }

    @Test
    public void producesSomethingTheRealParserUnderstands() {
        RegistryObject registryObject = TestVdf.parseWithoutCollapse("""
                "common"
                {
                  "gameid" "4242"
                }
                "ufs"
                {
                  "savefiles"
                  {
                    "0"
                    {
                      "root" "WinAppDataLocalLow"
                    }
                  }
                }
                """);

        assertThat(registryObject.getObjectValueAsString("common/gameid").getValue())
                .isEqualTo("4242");
        assertThat(registryObject.getObjectValueAsString("ufs/savefiles/0/root").getValue())
                .isEqualTo("WinAppDataLocalLow");
    }
}
