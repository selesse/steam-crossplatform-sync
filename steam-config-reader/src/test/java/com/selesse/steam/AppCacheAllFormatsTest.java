package com.selesse.steam;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.os.Resources;
import com.selesse.steam.appcache.AppCache;
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

    public AppCacheAllFormatsTest(String fileName) {
        this.fileName = fileName;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {{"appinfo.vdf"}, {"appinfo-pre-dec-2022.vdf"}, {"appinfo-magic-28.vdf"}});
    }

    @Test
    public void testCanParse() {
        Path path = Resources.getResource(fileName);
        AppCache appCache = new AppCacheReader().load(path);
        assertThat(appCache).isNotNull();
        assertThat(appCache.size()).isPositive();
    }
}
