package com.selesse.steam.registry.windows;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class GetGameIdFromGameOverlayTest {
    @Test
    public void parsesGameIdWhenItIsTheLastArgument() {
        // Get-CimInstance emits the bare command line, with no trailing padding (unlike wmic).
        String output = "\"C:/Program Files (x86)/Steam/gameoverlayui64.exe\" -pid 34704 -steampid 23500"
                + " -manuallyclearframes 0 -gameid 4078430\n";

        assertThat(GetGameIdFromGameOverlay.parseGameId(output)).isEqualTo(4078430L);
    }

    @Test
    public void parsesGameIdFollowedByMoreArguments() {
        assertThat(GetGameIdFromGameOverlay.parseGameId("overlay.exe -gameid 440 -pid 1"))
                .isEqualTo(440L);
    }

    @Test
    public void returnsZeroWhenThereIsNoGameId() {
        assertThat(GetGameIdFromGameOverlay.parseGameId("\n")).isEqualTo(0L);
    }
}
