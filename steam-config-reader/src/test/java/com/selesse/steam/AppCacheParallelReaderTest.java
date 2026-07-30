package com.selesse.steam;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.os.Resources;
import com.selesse.steam.appcache.App;
import com.selesse.steam.appcache.AppCache;
import com.selesse.steam.appcache.AppCacheBufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AppCacheParallelReaderTest {

    private final String fileName;
    private final int expectedAppCount;

    public AppCacheParallelReaderTest(String fileName, int expectedAppCount) {
        this.fileName = fileName;
        this.expectedAppCount = expectedAppCount;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"appinfo.vdf", 3469},
            {"appinfo-pre-dec-2022.vdf", 1947},
            {"appinfo-magic-28.vdf", 2134}
        });
    }

    @Test
    public void readIsConsistentAcrossParallelismLevels() throws IOException {
        Path path = Resources.getResource(fileName);
        // parallelism=1 is the simplest case (single chunk, no concurrency) and serves as the
        // ground truth that every other thread count is checked against.
        AppCache baseline = new AppCacheBufferedReader(path).read(1);
        assertThat(baseline.size()).isEqualTo(expectedAppCount);

        for (int parallelism : new int[] {2, 3, 8}) {
            AppCache result = new AppCacheBufferedReader(path).read(parallelism);
            assertThat(result.size())
                    .as("parallelism=%d for %s", parallelism, fileName)
                    .isEqualTo(expectedAppCount);

            // Cheap scalar-field comparison across every app: confirms the index/chunking/merge
            // logic located and reassembled every entry correctly.
            for (App expected : baseline.getApps()) {
                App actual = result.getById(expected.appId());
                String context = "appId=" + expected.appId() + ", parallelism=" + parallelism + " for " + fileName;
                assertThat(actual).as(context).isNotNull();
                assertThat(actual.size()).as(context).isEqualTo(expected.size());
                assertThat(actual.infoState()).as(context).isEqualTo(expected.infoState());
                assertThat(actual.lastUpdated()).as(context).isEqualTo(expected.lastUpdated());
                assertThat(actual.picsToken()).as(context).isEqualTo(expected.picsToken());
                assertThat(actual.changeNumber()).as(context).isEqualTo(expected.changeNumber());
                assertThat(actual.sha1()).as(context).containsExactly(expected.sha1());
                if (expected.sha1Binary() == null) {
                    assertThat(actual.sha1Binary()).as(context).isNull();
                } else {
                    assertThat(actual.sha1Binary()).as(context).containsExactly(expected.sha1Binary());
                }
            }

            // Expensive deep structural comparison (recursive over the whole VDF tree) is only
            // affordable for a spot check, not all ~thousands of apps across every parallelism
            // level - the scalar checks above already confirm every entry was located correctly.
            App expectedFirst = baseline.getApps().iterator().next();
            App actualFirst = result.getById(expectedFirst.appId());
            assertThat(actualFirst)
                    .as(
                            "deep comparison for appId=%d, parallelism=%d for %s",
                            expectedFirst.appId(), parallelism, fileName)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedFirst);
        }
    }
}
