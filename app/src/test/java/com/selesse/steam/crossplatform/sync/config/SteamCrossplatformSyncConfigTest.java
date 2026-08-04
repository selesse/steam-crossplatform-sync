package com.selesse.steam.crossplatform.sync.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.selesse.steam.crossplatform.sync.cloud.CloudSyncLocationSupplier;
import com.selesse.steam.crossplatform.sync.serialize.ConfigRaw;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.Test;

public class SteamCrossplatformSyncConfigTest {
    private static SteamCrossplatformSyncConfig configWith(ConfigRaw raw, CloudSyncLocationSupplier supplier) {
        return new SteamCrossplatformSyncConfig(Path.of("unused-config-dir"), raw, supplier);
    }

    // Regression test: getLocalCloudSyncBaseDirectory() used to build its fallback value with
    // Optional.orElse(...), whose argument Java evaluates eagerly regardless of whether the
    // configured path was present. That meant a configured pathToCloudStorage still paid for -
    // and could be broken by - cloud-storage provider auto-detection that was never going to be
    // used. This asserts the fallback is never even evaluated when a path is configured.
    @Test
    public void configuredPathToCloudStorageSkipsProviderAutoDetectionEntirely() {
        ConfigRaw raw = new ConfigRaw();
        raw.pathToCloudStorage = "/configured/cloud/storage";

        CloudSyncLocationSupplier supplier = mock(CloudSyncLocationSupplier.class);
        doThrow(new AssertionError("cloud storage auto-detection should not run when pathToCloudStorage is configured"))
                .when(supplier)
                .get(any(), any());

        Path result = configWith(raw, supplier).getLocalCloudSyncBaseDirectory();

        assertThat(result).isEqualTo(Path.of("/configured/cloud/storage"));
    }

    // Auto-detection used to be reached through a static call taking the whole config, which once
    // silently received a bogus substitute because the config interface happened to be
    // structurally compatible with a functional interface. The supplier is handed the two values
    // it actually needs now, so there is nothing left to mix up - this covers the behaviour.
    @Test
    public void missingPathToCloudStorageFallsBackToAutoDetection() {
        CloudSyncLocationSupplier supplier = mock(CloudSyncLocationSupplier.class);
        doReturn(Optional.of(Path.of("/detected/cloud/root"))).when(supplier).get(any(), any());

        Path result = configWith(new ConfigRaw(), supplier).getLocalCloudSyncBaseDirectory();

        assertThat(result).isEqualTo(Path.of("/detected/cloud/root"));
    }

    // When nothing is configured and auto-detection also finds nothing, the failure used to
    // surface as a bare NoSuchElementException with no indication of what to actually do about
    // it. This asserts it now names the config file to edit.
    @Test
    public void noCloudStorageFoundGivesAnActionableErrorMessage() {
        CloudSyncLocationSupplier supplier = mock(CloudSyncLocationSupplier.class);
        doReturn(Optional.empty()).when(supplier).get(any(), any());

        SteamCrossplatformSyncConfig config = configWith(new ConfigRaw(), supplier);

        assertThatThrownBy(config::getLocalCloudSyncBaseDirectory)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pathToCloudStorage")
                .hasMessageContaining(
                        config.getConfigFileLocation().toAbsolutePath().toString());
    }
}
