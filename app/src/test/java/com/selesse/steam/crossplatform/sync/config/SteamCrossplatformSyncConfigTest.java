package com.selesse.steam.crossplatform.sync.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import com.selesse.steam.crossplatform.sync.cloud.CloudSyncLocationSupplier;
import com.selesse.steam.crossplatform.sync.serialize.ConfigRaw;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.Test;
import org.mockito.MockedStatic;

public class SteamCrossplatformSyncConfigTest {
    private static class TestConfig implements SteamCrossplatformSyncConfig {
        @Override
        public Path getConfigDirectory() {
            return Path.of("unused-config-dir");
        }
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

        try (MockedStatic<ConfigLoader> configLoader = mockStatic(ConfigLoader.class);
                MockedStatic<CloudSyncLocationSupplier> cloudSupplier = mockStatic(CloudSyncLocationSupplier.class)) {
            configLoader.when(() -> ConfigLoader.loadIfExists(any())).thenReturn(Optional.of(raw));
            cloudSupplier
                    .when(() -> CloudSyncLocationSupplier.get(any()))
                    .thenThrow(new AssertionError("CloudSyncLocationSupplier.get() should not be evaluated"
                            + " when pathToCloudStorage is already configured"));

            Path result = new TestConfig().getLocalCloudSyncBaseDirectory();

            assertThat(result).isEqualTo(Path.of("/configured/cloud/storage"));
        }
    }

    // Regression test: the auto-detection fallback used to call
    // CloudSyncLocationSupplier.get(this::getCloudStorageRelativeWritePath) - a method reference
    // that only compiled because SteamCrossplatformSyncConfig happens to be a structurally
    // compatible functional interface, not because it was the right argument. It silently passed
    // a bogus config substitute instead of the real config. This asserts the real config instance
    // is what actually gets passed through.
    @Test
    public void missingPathToCloudStorageFallsBackToAutoDetectionWithTheRealConfigInstance() {
        ConfigRaw raw = new ConfigRaw();
        TestConfig config = new TestConfig();

        try (MockedStatic<ConfigLoader> configLoader = mockStatic(ConfigLoader.class);
                MockedStatic<CloudSyncLocationSupplier> cloudSupplier = mockStatic(CloudSyncLocationSupplier.class)) {
            configLoader.when(() -> ConfigLoader.loadIfExists(any())).thenReturn(Optional.of(raw));
            cloudSupplier
                    .when(() -> CloudSyncLocationSupplier.get(any()))
                    .thenReturn(Optional.of(Path.of("/detected/cloud/root")));

            Path result = config.getLocalCloudSyncBaseDirectory();

            assertThat(result).isEqualTo(Path.of("/detected/cloud/root"));
            cloudSupplier.verify(() -> CloudSyncLocationSupplier.get(config));
        }
    }

    // When nothing is configured and auto-detection also finds nothing, the failure used to
    // surface as a bare NoSuchElementException with no indication of what to actually do about
    // it. This asserts it now names the config file to edit.
    @Test
    public void noCloudStorageFoundGivesAnActionableErrorMessage() {
        ConfigRaw raw = new ConfigRaw();
        TestConfig config = new TestConfig();

        try (MockedStatic<ConfigLoader> configLoader = mockStatic(ConfigLoader.class);
                MockedStatic<CloudSyncLocationSupplier> cloudSupplier = mockStatic(CloudSyncLocationSupplier.class)) {
            configLoader.when(() -> ConfigLoader.loadIfExists(any())).thenReturn(Optional.of(raw));
            cloudSupplier.when(() -> CloudSyncLocationSupplier.get(any())).thenReturn(Optional.empty());

            assertThatThrownBy(config::getLocalCloudSyncBaseDirectory)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("pathToCloudStorage")
                    .hasMessageContaining(
                            config.getConfigFileLocation().toAbsolutePath().toString());
        }
    }
}
