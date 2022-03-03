package com.selesse.steam.crossplatform.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
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
        SteamCrossplatformSyncContext context = new SteamCrossplatformSyncContext();
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
}
