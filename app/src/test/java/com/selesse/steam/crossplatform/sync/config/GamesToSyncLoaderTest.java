package com.selesse.steam.crossplatform.sync.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

public class GamesToSyncLoaderTest {
    // Regression test: SyncGameFilesService.run() used to call config.getGamesFile() itself (to
    // log the path) and then GamesToSyncLoader.loadGames() called it again internally - resolving
    // it twice per sync. Since getGamesFile() can trigger cloud-storage provider auto-detection,
    // that doubled a potentially expensive/fallible lookup for no reason. The path is now
    // resolved once, here, and the surrounding log lines reused it instead of calling it again.
    @Test
    public void resolvesGamesFileExactlyOnce() throws IOException {
        Path gamesFile = Files.createTempFile("games", ".yml");
        gamesFile.toFile().deleteOnExit();
        Files.writeString(gamesFile, "games: []\n");

        SteamCrossplatformSyncConfig config = mock(SteamCrossplatformSyncConfig.class);
        doReturn(gamesFile).when(config).getGamesFile();

        var gameList = new GamesToSyncLoader().loadGames(config);

        assertThat(gameList.getGames()).isEmpty();
        verify(config).getGamesFile();
    }
}
