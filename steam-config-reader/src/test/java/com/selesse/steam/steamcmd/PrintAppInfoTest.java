package com.selesse.steam.steamcmd;

import com.selesse.os.Resources;
import com.selesse.steam.registry.implementation.RegistryStore;
import com.selesse.steam.registry.implementation.RegistryString;
import org.assertj.core.util.Lists;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class PrintAppInfoTest {
    private static class LocalFileReaderAppInfo extends PrintAppInfoExecutor {
        @Override
        public List<String> runPrintAppInfoProcess(Long appId) {
            Path fakeFilePath = Resources.getResource(appId + ".vdf");
            try {
                return Files.readAllLines(fakeFilePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Map<Long, List<String>> runPrintAppInfoProcesses(List<Long> appIds) {
            return appIds.stream().collect(Collectors.toMap(Function.identity(), this::runPrintAppInfoProcess));
        }
    }

    @Test
    public void canParseRegistryStoreFromFile() {
        long appId = 457140;
        PrintAppInfo appInfo = new PrintAppInfo();
        RegistryStore registryStore = appInfo.getRegistryStore(new LocalFileReaderAppInfo(), appId);

        RegistryString appName = registryStore.getObjectValueAsString("common/name");
        assertThat(appName.getValue()).isEqualTo("Oxygen Not Included");
    }

    @Test
    public void canPrintMultipleAppInfo() {
        Long hollowKnightId = (long) 367520;
        Long oxygenNotIncludedId = (long) 457140;
        PrintAppInfo appInfo = new PrintAppInfo();
        Map<Long, RegistryStore> registryStores =
                appInfo.getRegistryStores(new LocalFileReaderAppInfo(),
                        Lists.newArrayList(hollowKnightId, oxygenNotIncludedId));

        RegistryStore hollowKnightStore = registryStores.get(hollowKnightId);
        assertThat(getApplicationName(hollowKnightStore)).isEqualTo("Hollow Knight");

        RegistryStore oxygenNotIncludedStore = registryStores.get(oxygenNotIncludedId);
        assertThat(getApplicationName(oxygenNotIncludedStore)).isEqualTo("Oxygen Not Included");
    }

    private String getApplicationName(RegistryStore store) {
        return store.getObjectValueAsString("common/name").getValue();
    }
}