package com.selesse.steamcrossplatformsync.gamesessions.database;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.resource.LoadableResource;

/**
 * The migration scripts this module carries, handed to Flyway directly rather than found by
 * scanning.
 *
 * <p>Flyway locates migrations by scanning a classpath for {@code db/migration}. That works from a
 * jar or an exploded directory, but a jlink image has no classpath: the scripts live in the {@code
 * jrt:} runtime image, where the scanner enumerates nothing. Flyway reads "no scripts anywhere" as
 * "nothing to apply", so a fresh install through the jlink image produced a database containing
 * only Flyway's own schema history table.
 *
 * <p>A named module can list its own contents through {@link ModuleReader} whatever it is packaged
 * in, so that is what this uses. Enumerating rather than hardcoding means new migrations need no
 * registration here — dropping a script in {@code db/migration} remains the whole story.
 */
final class ModuleMigrationScripts implements ResourceProvider {
    private static final String LOCATION = "db/migration";
    private static final String SUFFIX = ".sql";

    private final List<LoadableResource> scripts;

    private ModuleMigrationScripts(List<LoadableResource> scripts) {
        this.scripts = scripts;
    }

    /**
     * The scripts held by this module, or empty when they can't be listed that way — because the
     * code is on a classpath rather than a module path, or because the module turned out not to
     * carry them. Both cases leave Flyway to scan as it always has, so this can only add a way of
     * finding migrations, never take one away.
     */
    static Optional<ResourceProvider> forThisModule() {
        Module module = ModuleMigrationScripts.class.getModule();
        ModuleLayer layer = module.getLayer();
        if (!module.isNamed() || layer == null) {
            return Optional.empty();
        }
        return layer.configuration().findModule(module.getName()).flatMap(resolved -> from(resolved.reference()));
    }

    /**
     * The scripts carried by {@code module}, or empty when it carries none. Separate from {@link
     * #forThisModule()} so a test can point it at a module other than the one it is running in.
     */
    static Optional<ResourceProvider> from(ModuleReference module) {
        List<LoadableResource> scripts = read(module);
        return scripts.isEmpty() ? Optional.empty() : Optional.of(new ModuleMigrationScripts(scripts));
    }

    private static List<LoadableResource> read(ModuleReference module) {
        try (ModuleReader reader = module.open()) {
            List<String> names;
            try (Stream<String> contents = reader.list()) {
                names = contents.filter(name -> name.startsWith(LOCATION + "/") && name.endsWith(SUFFIX))
                        .sorted()
                        .toList();
            }

            // Read eagerly: Flyway pulls a script's contents long after this reader is closed.
            List<LoadableResource> scripts = new ArrayList<>();
            for (String name : names) {
                scripts.add(new Script(name, module.location().orElse(null), read(reader, name)));
            }
            return scripts;
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Could not read the migration scripts in "
                            + module.descriptor().name(),
                    e);
        }
    }

    private static String read(ModuleReader reader, String name) throws IOException {
        try (InputStream stream =
                reader.open(name).orElseThrow(() -> new IOException(name + " was listed but could not be opened"))) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Override
    public LoadableResource getResource(String name) {
        return scripts.stream()
                .filter(script -> script.getFilename().equals(name)
                        || script.getRelativePath().equals(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Collection<LoadableResource> getResources(String prefix, String[] suffixes) {
        return scripts.stream()
                .filter(script -> script.getFilename().startsWith(prefix))
                .filter(script -> Stream.of(suffixes).anyMatch(script.getFilename()::endsWith))
                .toList();
    }

    private static final class Script extends LoadableResource {
        private final String relativePath;
        private final String absolutePath;
        private final String contents;

        private Script(String relativePath, URI moduleLocation, String contents) {
            this.relativePath = relativePath;
            this.absolutePath = moduleLocation == null ? relativePath : moduleLocation + "/" + relativePath;
            this.contents = contents;
        }

        @Override
        public Reader read() {
            return new StringReader(contents);
        }

        @Override
        public String getAbsolutePath() {
            return absolutePath;
        }

        @Override
        public String getAbsolutePathOnDisk() {
            return absolutePath;
        }

        @Override
        public String getFilename() {
            return relativePath.substring(relativePath.lastIndexOf('/') + 1);
        }

        @Override
        public String getRelativePath() {
            return relativePath;
        }
    }
}
