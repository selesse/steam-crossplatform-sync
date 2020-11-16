package com.selesse.steam.steamcmd;

import com.google.common.io.Resources;
import com.selesse.steam.registry.implementation.RegistryStore;
import com.selesse.steam.registry.implementation.RegistryString;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PrintAppInfoTest {
    private static class FakeExecutor extends PrintAppInfoExecutor {
        @Override
        public List<String> runPrintAppInfoProcess(Long appId) {
            Path fakeFilePath = Path.of(Resources.getResource(appId + ".vdf").getPath());
            try {
                return Files.readAllLines(fakeFilePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    public void canParseRegistryStoreFromFile() {
        long appId = 457140;
        PrintAppInfo appInfo = new PrintAppInfo();
        RegistryStore registryStore = appInfo.getRegistryStore(new FakeExecutor(), appId);

        RegistryString appName = registryStore.getObjectValueAsString(appId + "/common/name");
        assertThat(appName.getValue()).isEqualTo("Oxygen Not Included");
    }
}