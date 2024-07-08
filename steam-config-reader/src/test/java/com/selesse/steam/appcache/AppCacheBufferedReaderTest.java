package com.selesse.steam.appcache;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.selesse.os.Resources;
import java.nio.file.Path;
import java.util.Formatter;
import org.junit.Test;

public class AppCacheBufferedReaderTest {
    @Test
    public void canParseMagicVersion29() throws Exception {
        AppCache appCache = loadAppCache(Resources.getResource("appinfo.vdf"));

        var app = appCache.getById(5);

        assertThat(app.appId()).isEqualTo(5);
        assertThat(app.infoState()).isEqualTo(1);
        assertThat(app.size()).isEqualTo(85);
        // this should be 1709081169??
        assertThat(app.lastUpdated()).isEqualTo(1594076880);
        assertThat(app.changeNumber()).isEqualTo(22534048);
        assertThat(app.picsToken()).isEqualTo(0);
        assertThat(byteArrayToHex(app.sha1())).isEqualTo("87fa436785800db490496ddc7db481ee518b8235");

        VdfObject object = app.vdfObject();

        assertThat(object.getName()).isEqualTo("appinfo");
        assertThat(object.getValues().get(0)).isEqualTo(new VdfInteger("appid", 5));
        assertThat(object.getValues().get(1)).isEqualTo(new VdfInteger("public_only", 1));
    }

    @Test
    public void canParseMagicVersion28() throws Exception {
        AppCache appCache = loadAppCache(Resources.getResource("appinfo-magic-28.vdf"));

        var app = appCache.getById(5);

        assertThat(app.appId()).isEqualTo(5);
        assertThat(app.infoState()).isEqualTo(1);
        assertThat(app.picsToken()).isEqualTo(0);

        assertThat(app.size()).isEqualTo(99);
        assertThat(app.lastUpdated()).isEqualTo(1660750659);
        assertThat(app.changeNumber()).isEqualTo(17072555);
        assertThat(byteArrayToHex(app.sha1())).isEqualTo("87fa436785800db490496ddc7db481ee518b8235");

        VdfObject object = app.vdfObject();

        assertThat(object.getName()).isEqualTo("appinfo");
        assertThat(object.getValues().get(0)).isEqualTo(new VdfInteger("appid", 5));
        assertThat(object.getValues().get(1)).isEqualTo(new VdfInteger("public_only", 1));
    }

    @Test
    public void canParseMagicVersion27() throws Exception {
        AppCache appCache = loadAppCache(Resources.getResource("appinfo-pre-dec-2022.vdf"));

        var app = appCache.getById(5);

        assertThat(app.appId()).isEqualTo(5);
        assertThat(app.infoState()).isEqualTo(1);
        assertThat(app.picsToken()).isEqualTo(0);

        assertThat(app.size()).isEqualTo(79);
        assertThat(app.lastUpdated()).isEqualTo(1617940879);
        assertThat(app.changeNumber()).isEqualTo(13224007);
        assertThat(byteArrayToHex(app.sha1())).isEqualTo("87fa436785800db490496ddc7db481ee518b8235");

        VdfObject object = app.vdfObject();

        assertThat(object.getName()).isEqualTo("appinfo");
        assertThat(object.getValues().get(0)).isEqualTo(new VdfInteger("appid", 5));
        assertThat(object.getValues().get(1)).isEqualTo(new VdfInteger("public_only", 1));
    }

    private AppCache loadAppCache(Path path) throws Exception {
        var appCacheBufferedReader = new AppCacheBufferedReader(path);
        return appCacheBufferedReader.call();
    }

    private static String byteArrayToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}