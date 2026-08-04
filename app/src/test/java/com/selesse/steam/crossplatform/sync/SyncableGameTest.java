package com.selesse.steam.crossplatform.sync;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

// SyncableGame is both the in-memory type and the games.yml binding, so a helper method added to
// it can silently change the file's shape - Jackson treats getters as properties. These tests pin
// the written shape: they fail if the property whitelist, the field order or NON_NULL is dropped.
public class SyncableGameTest {
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    @Test
    public void writesOnlyTheStoredFieldsInAFixedOrder() {
        GameConfig config = new GameConfig(List.of(new SyncableGame(
                "Hollow Knight", List.of("%USERPROFILE%/hk"), List.of("~/hk"), List.of("~/.hk"), 367520L, true)));

        assertThat(MAPPER.writeValueAsString(config)).isEqualTo("""
                        ---
                        games:
                        - name: "Hollow Knight"
                          gameId: 367520
                          windows:
                          - "%USERPROFILE%/hk"
                          mac:
                          - "~/hk"
                          linux:
                          - "~/.hk"
                          sync: true
                        """);
    }

    @Test
    public void omitsTheKeyForAnOsTheGameDoesNotRunOn() {
        GameConfig config = new GameConfig(
                List.of(new SyncableGame("Windows Only", List.of("%USERPROFILE%/wo"), null, null, 1L, true)));

        assertThat(MAPPER.writeValueAsString(config)).doesNotContain("mac", "linux");
    }

    @Test
    public void readsBackWhatItWrote() {
        GameConfig config = new GameConfig(List.of(
                new SyncableGame("Hollow Knight", List.of("%USERPROFILE%/hk"), List.of("~/hk"), null, 367520L, true),
                new SyncableGame(
                        "Baba Is You", List.of("%USERPROFILE%/baba"), null, List.of("~/.baba"), 736260L, false)));

        GameConfig reread = MAPPER.readValue(MAPPER.writeValueAsString(config), GameConfig.class);

        assertThat(reread).isEqualTo(config);
        assertThat(reread.getGame(736260L)).contains(config.games().get(1));
    }

    @Test
    public void ignoresKeysItDoesNotRecognise() {
        String yaml = """
                games:
                - name: "Hollow Knight"
                  gameId: 367520
                  windows:
                  - "%USERPROFILE%/hk"
                  sync: true
                  somethingAddedByAFutureVersion: true
                """;

        GameConfig config = MAPPER.readValue(yaml, GameConfig.class);

        assertThat(config.games()).hasSize(1);
        assertThat(config.games().get(0).name()).isEqualTo("Hollow Knight");
    }
}
