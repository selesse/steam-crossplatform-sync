package com.selesse.steam.games;

import com.selesse.files.FileVisitors;
import com.selesse.steam.registry.SteamRegistry;
import java.nio.file.Path;
import java.util.List;

public class LibraryCacheInstalledGameFinder implements InstalledGameFetcher {
    @Override
    public List<Long> fetch() {
        Path libraryCachePath = SteamRegistry.getInstance().getLibraryCachePath();
        List<Path> paths = FileVisitors.listAll(libraryCachePath);
        return paths.stream()
                .filter(x -> x.getFileName().toString().matches("[0-9]+_.*+"))
                .map(x -> x.getFileName().toString().split("_")[0])
                .distinct()
                .map(Long::parseLong)
                .toList();
    }
}
