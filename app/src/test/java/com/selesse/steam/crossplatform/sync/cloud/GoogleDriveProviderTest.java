package com.selesse.steam.crossplatform.sync.cloud;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class GoogleDriveProviderTest {
    @Test
    public void driveProbesRunOnDedicatedDaemonThreadsNotTheCommonPool() throws Exception {
        Future<String> future = GoogleDriveProvider.DRIVE_PROBE_EXECUTOR.submit(
                () -> Thread.currentThread().getName());

        assertThat(future.get(5, TimeUnit.SECONDS)).startsWith("google-drive-probe");
    }
}
