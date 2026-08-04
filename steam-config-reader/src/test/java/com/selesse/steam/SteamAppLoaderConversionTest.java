package com.selesse.steam;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.selesse.os.Resources;
import com.selesse.steam.appcache.App;
import com.selesse.steam.appcache.AppCache;
import com.selesse.steam.registry.SteamRegistry;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

// Every other SteamAppLoader test exercises convert() for a handful of named games. This walks
// every app in each real fixture format through convert(), so a VDF field type Steam starts
// using that we don't handle yet gets caught here instead of by a user in the wild.
@RunWith(Parameterized.class)
public class SteamAppLoaderConversionTest {

    private final String fileName;

    public SteamAppLoaderConversionTest(String fileName) {
        this.fileName = fileName;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {{"appinfo.vdf"}, {"appinfo-pre-dec-2022.vdf"}, {"appinfo-magic-28.vdf"}});
    }

    @Test
    public void everyAppConvertsWithoutThrowing() {
        Path path = Resources.getResource(fileName);
        AppCache appCache = new AppCacheReader(new SteamRegistry()).load(path);

        for (App app : appCache.getApps()) {
            assertThatCode(() -> SteamAppLoader.convert(app.vdfObject()))
                    .as("app ID %d should convert without throwing", app.appId())
                    .doesNotThrowAnyException();
        }
    }
}
