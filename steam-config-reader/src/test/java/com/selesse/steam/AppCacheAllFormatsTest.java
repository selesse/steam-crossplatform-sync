package com.selesse.steam;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.os.Resources;
import com.selesse.steam.appcache.AppCache;
import com.selesse.steam.registry.SteamRegistry;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AppCacheAllFormatsTest {

    private final String fileName;
    private final int expectedAppCount;

    public AppCacheAllFormatsTest(String fileName, int expectedAppCount) {
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
    public void testCanParse() {
        Path path = Resources.getResource(fileName);
        AppCache appCache = new AppCacheReader(new SteamRegistry()).load(path);
        assertThat(appCache).isNotNull();
        assertThat(appCache.size()).isEqualTo(expectedAppCount);
    }
}
