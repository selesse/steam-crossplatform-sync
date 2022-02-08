package com.selesse.steam;

import com.google.common.base.Joiner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class RegistryStores {
    public static void cacheRegistryStore(Path cacheDirectory, long gameId, List<String> gameRegistryLines) {
        if (!cacheDirectory.toFile().isDirectory()) {
            cacheDirectory.toFile().mkdirs();
        }
        Path file = Path.of(cacheDirectory.toAbsolutePath().toString(), "/" + gameId + ".vdf");
        try {
            Files.write(file, gameRegistryLines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<List<String>> readCache(Path cacheDirectory, long gameId) {
        Path file = Path.of(cacheDirectory.toAbsolutePath().toString(), "/" + gameId + ".vdf");
        if (file.toFile().exists()) {
            try {
                List<String> lines = Files.readAllLines(file);
                if (isEmptyCache(gameId, lines)) {
                    return Optional.empty();
                }
                return Optional.of(lines);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.empty();
    }

    private static boolean isEmptyCache(long gameId, List<String> lines) {
        return lines.size() == 3 && Joiner.on(" ").join(lines).equals("\"" + gameId + "\" { }");
    }
}
