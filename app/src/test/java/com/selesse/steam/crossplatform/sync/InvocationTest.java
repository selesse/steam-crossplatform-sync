package com.selesse.steam.crossplatform.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.Test;

public class InvocationTest {
    @Test
    public void noArgumentsRunsTheDaemon() {
        Invocation invocation = Invocation.parse(List.of());

        assertThat(invocation.command()).isEqualTo(Command.DAEMON);
        assertThat(invocation.fast()).isFalse();
        assertThat(invocation.appIds()).isEmpty();
    }

    @Test
    public void fastAppliesToTheDaemon() {
        assertThat(Invocation.parse(List.of("--fast")).fast()).isTrue();
    }

    @Test
    public void readsAppIdsFollowingACommand() {
        Invocation invocation = Invocation.parse(List.of("--sync", "367520", "1236720"));

        assertThat(invocation.command()).isEqualTo(Command.SYNC);
        assertThat(invocation.appIds()).containsExactly(367520L, 1236720L);
    }

    @Test
    public void commandsTakingNoAppIdsNeedNone() {
        assertThat(Invocation.parse(List.of("--print-games")).appIds()).isEmpty();
    }

    // The bug this whole parser exists for: anything unrecognized used to fall through to the
    // daemon, so a typo started a background process rather than saying what was wrong.
    @Test
    public void rejectsAnUnknownOption() {
        assertThatThrownBy(() -> Invocation.parse(List.of("--sink")))
                .isInstanceOf(UsageException.class)
                .hasMessage("Unknown option --sink");
    }

    @Test
    public void rejectsTwoCommandsAtOnce() {
        assertThatThrownBy(() -> Invocation.parse(List.of("--print-games", "--sync")))
                .isInstanceOf(UsageException.class)
                .hasMessageContaining("Only one command at a time");
    }

    @Test
    public void acceptsTheSameCommandTwice() {
        assertThat(Invocation.parse(List.of("--sync", "--sync")).command()).isEqualTo(Command.SYNC);
    }

    @Test
    public void rejectsAnAppIdThatIsntANumber() {
        assertThatThrownBy(() -> Invocation.parse(List.of("--print-game", "Hollow Knight")))
                .isInstanceOf(UsageException.class)
                .hasMessage("Not a Steam app ID: Hollow Knight");
    }

    @Test
    public void rejectsAppIdsWithoutACommand() {
        assertThatThrownBy(() -> Invocation.parse(List.of("367520")))
                .isInstanceOf(UsageException.class)
                .hasMessageContaining("No command given");
    }

    @Test
    public void rejectsAppIdsForACommandThatTakesNone() {
        assertThatThrownBy(() -> Invocation.parse(List.of("--list-app-ids", "367520")))
                .isInstanceOf(UsageException.class)
                .hasMessage("--list-app-ids takes no app IDs");
    }

    @Test
    public void rejectsACommandMissingItsRequiredAppIds() {
        assertThatThrownBy(() -> Invocation.parse(List.of("--print-game")))
                .isInstanceOf(UsageException.class)
                .hasMessage("--print-game requires at least one app ID");
    }

    // --fast used to be read only on the branch that starts the daemon, so pairing it with any
    // other command quietly did nothing.
    @Test
    public void rejectsFastOutsideTheDaemon() {
        assertThatThrownBy(() -> Invocation.parse(List.of("--sync", "--fast")))
                .isInstanceOf(UsageException.class)
                .hasMessageContaining("--fast");
    }

    @Test
    public void usageListsEveryCommandAndOption() {
        String usage = Command.usage();

        assertThat(usage).contains("Usage: steam-crossplatform-sync");
        for (Command command : Command.values()) {
            if (command != Command.DAEMON) {
                assertThat(usage).contains(command.displayName());
            }
        }
        assertThat(usage).contains(Command.FAST_FLAG);
    }
}
