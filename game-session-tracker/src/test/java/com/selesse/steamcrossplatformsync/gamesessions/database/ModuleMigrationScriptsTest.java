package com.selesse.steamcrossplatformsync.gamesessions.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.resource.LoadableResource;
import org.junit.Before;
import org.junit.Test;

/**
 * Points the provider at the jar this project builds, which is a real named module whatever the
 * tests themselves are running on. Tests run from the classpath, so {@code forThisModule()} would
 * find nothing here — the module to read has to be supplied.
 *
 * <p>This does not cover the jlink image, where the module is read out of a {@code jrt:} filesystem
 * rather than a jar. That needs the image itself, and is checked by hand for now.
 */
public class ModuleMigrationScriptsTest {
    private static final String BASE_TABLES = "V20221118200241__create-base-tables.sql";
    private static final String SQL = ".sql";

    private ResourceProvider scripts;

    @Before
    public void setup() {
        Path jar = Path.of(System.getProperty("game-session-tracker.jar"));
        ModuleReference module = ModuleFinder.of(jar)
                .find("com.selesse.steamcrossplatformsync.gamesessions")
                .orElseThrow();
        scripts = ModuleMigrationScripts.from(module).orElseThrow();
    }

    @Test
    public void listsTheMigrationScriptsTheModuleCarries() {
        assertThat(filenames())
                .contains(
                        BASE_TABLES, "V20221126152503__use-steam-ids.sql", "V20260131000000__add-active-playtime.sql");
    }

    @Test
    public void everyScriptFoundIsAMigration() {
        assertThat(all()).allSatisfy(script -> {
            assertThat(script.getRelativePath()).startsWith("db/migration/");
            assertThat(script.getFilename()).endsWith(SQL);
        });
    }

    @Test
    public void handsScriptsBackInVersionOrder() {
        assertThat(filenames()).isSortedAccordingTo(Comparator.naturalOrder());
    }

    /** The reader the scripts were listed through is closed by the time Flyway asks to read one. */
    @Test
    public void readsScriptContentsAfterTheModuleReaderIsClosed() {
        assertThat(contentsOf(scripts.getResource(BASE_TABLES)))
                .contains("CREATE TABLE games")
                .contains("CREATE TABLE gaming_sessions");
    }

    @Test
    public void ignoresPrefixesAndSuffixesFlywayDidNotAskFor() {
        assertThat(scripts.getResources("R", new String[] {SQL})).isEmpty();
        assertThat(scripts.getResources("V", new String[] {".conf"})).isEmpty();
    }

    @Test
    public void findsAScriptByFilenameOrByRelativePath() {
        assertThat(scripts.getResource(BASE_TABLES)).isNotNull();
        assertThat(scripts.getResource("db/migration/" + BASE_TABLES)).isNotNull();
    }

    @Test
    public void doesNotFindAScriptItDoesNotHave() {
        assertThat(scripts.getResource("V19990101000000__invented.sql")).isNull();
    }

    private List<LoadableResource> all() {
        return List.copyOf(scripts.getResources("V", new String[] {SQL}));
    }

    private List<String> filenames() {
        return all().stream().map(LoadableResource::getFilename).toList();
    }

    private String contentsOf(LoadableResource script) {
        try (BufferedReader reader = new BufferedReader(script.read())) {
            return reader.lines().reduce("", (a, b) -> a + "\n" + b);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
