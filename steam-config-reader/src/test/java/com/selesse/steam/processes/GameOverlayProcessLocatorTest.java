package com.selesse.steam.processes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class GameOverlayProcessLocatorTest {
    @Test
    public void readsTheAppIdFromTheGameIdArgument() {
        String[] args = {"-pid", "52095", "-steampid", "39700", "-manuallyclearframes", "0", "-gameid", "646570"};

        assertThat(GameOverlayProcessLocator.appIdFromGameIdArgument(args)).isEqualTo(646570L);
    }

    @Test
    public void gameIdArgumentWithoutAValueIsNoGame() {
        assertThat(GameOverlayProcessLocator.appIdFromGameIdArgument(new String[] {"-gameid"}))
                .isZero();
    }

    @Test
    public void readsTheAppIdFromAnAppIdArgument() {
        String[] args = {"SteamLaunch", "AppId=646570", "--", "/path/to/game"};

        assertThat(GameOverlayProcessLocator.appIdFromAppIdArgument(args)).isEqualTo(646570L);
    }

    @Test
    public void noAppIdArgumentIsNoGame() {
        assertThat(GameOverlayProcessLocator.appIdFromAppIdArgument(new String[] {"-pid", "1"}))
                .isZero();
    }
}
