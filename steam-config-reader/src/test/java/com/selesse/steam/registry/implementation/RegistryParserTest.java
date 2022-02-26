package com.selesse.steam.registry.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Splitter;
import java.util.List;
import org.junit.Test;

public class RegistryParserTest {
    @Test
    public void canParseRegistryWithSpecialCharacter() {
        String specialKeyName = String.format("\"%s\"", "hello\t");
        String specialValue = String.format("\"%s\"", "my \tfriend\\\"");
        List<String> lines = List.of(String.format("\t\t%s\t\t%s", specialKeyName, specialValue));
        RegistryStore registry = RegistryParser.parse(lines);

        RegistryValue objectValue = registry.getObjectValue("hello\t");
        assertThat(objectValue).isInstanceOf(RegistryString.class);

        assertThat(((RegistryString) objectValue).getName()).isEqualTo("hello\t");
        assertThat(((RegistryString) objectValue).getValue()).isEqualTo("my \tfriend\\\"");
    }

    @Test
    public void canParseGnarlyMultilineStrings() {
        String value =
                """
                \t\t\t\t"string"
                \t\t\t\t{
                \t\t\t\t\t"admin_rights"\t\t"No administrator rights."
                \t\t\t\t\t"analysis_disclaimer"\t\t"Dear Software User,

                This test program has been developed with your personal interest in mind to check for possible hardware and/or software incompatibility on your PC. To shorten the analysis time, system information is collected (similar to the Microsoft's msinfo32.exe program).
                Data will be compared with our knowledge base to discover hardware/software conflicts. Submitting the log file is totally voluntary. The collected data is for evaluation purposes only and is not used in any other manner.
                Your Support Team
                Do you want to start?"
                \t\t\t\t\t"analysis_done"\t\t"The Information was successfully collected and stored to the following file
                "
                \t\t\t\t\t"%file%"\t\t"
                Please contact Customer Support for forwarding instructions."
                \t\t\t\t\t"dawnofwar_ver"\t\t"1.51"
                \t\t\t\t}
                """;
        RegistryStore registryStore = RegistryParser.parse(Splitter.on("\n").splitToList(value));
        assertThat(registryStore.getObjectValueAsObject("string").getKeys())
                .contains("admin_rights", "analysis_disclaimer", "analysis_done", "%file%", "dawnofwar_ver");
    }
}
