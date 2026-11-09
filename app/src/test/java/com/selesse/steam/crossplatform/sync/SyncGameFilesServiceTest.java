package com.selesse.steam.crossplatform.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.selesse.files.FileVisitors;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;

public class SyncGameFilesServiceTest {
    @Test
    public void canHandleGameWithGlob() throws IOException {
        SteamCrossplatformSyncContext context = mock(SteamCrossplatformSyncContext.class);
        doReturn(null).when(context).getConfig();
        SyncGameFilesService syncGameFilesService = new SyncGameFilesService(context);

        Path tempLocalPath = Files.createTempDirectory("sync-test-local");
        Path tempFile = Files.createTempFile(tempLocalPath, "bob", ".dat");

        Path tempCloudPath = Files.createTempDirectory("sync-test-cloud");

        SyncableGame gameSupportedOnAllOses = spy(new SyncableGame(
                "Hollow Knight",
                List.of(tempLocalPath.toAbsolutePath() + "/*.dat"),
                List.of(tempLocalPath.toAbsolutePath() + "/*.dat"),
                List.of(tempLocalPath.toAbsolutePath() + "/*.dat"),
                367520L,
                true));
        doReturn(tempCloudPath).when(gameSupportedOnAllOses).getLocalCloudSyncPath(any());
        syncGameFilesService.sync(gameSupportedOnAllOses);

        assertThat(FileVisitors.listAll(tempCloudPath))
                .containsExactly(Path.of(
                        tempCloudPath.toAbsolutePath().toString(),
                        tempFile.getFileName().toString()));
    }

    // The folder is named after an account other than whoever might be signed in: that it's a
    // Steam account id is what keeps it, so the cloud copy doesn't move when accounts switch.
    @Test
    public void keepsAPerAccountSaveFolderInTheCloudCopy() throws IOException {
        SteamCrossplatformSyncContext context = mock(SteamCrossplatformSyncContext.class);
        SyncGameFilesService syncGameFilesService = new SyncGameFilesService(context);

        Path accountFolder = Files.createDirectory(
                Files.createTempDirectory("sync-test-local").resolve("76561199039324428"));
        Path tempFile = Files.createTempFile(accountFolder, "bob", ".sav");
        Path tempCloudPath = Files.createTempDirectory("sync-test-cloud");

        String localPath = accountFolder.toAbsolutePath() + "/*.sav";
        SyncableGame game = spy(
                new SyncableGame("Portal 2", List.of(localPath), List.of(localPath), List.of(localPath), 620L, true));
        doReturn(tempCloudPath).when(game).getLocalCloudSyncPath(any());
        syncGameFilesService.sync(game);

        assertThat(FileVisitors.listAll(tempCloudPath))
                .containsExactly(Path.of(
                        tempCloudPath.toAbsolutePath().toString(),
                        "76561199039324428",
                        tempFile.getFileName().toString()));
    }
}
