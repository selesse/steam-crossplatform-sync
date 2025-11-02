package com.selesse.steam;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.os.Resources;
import com.selesse.steam.appcache.*;
import java.nio.file.Path;
import java.util.Formatter;
import org.junit.Test;

public class AppCacheReaderTest {
    @Test
    public void testAppId5_preDec2022() {
        Path path = Resources.getResource("appinfo-pre-dec-2022.vdf");
        AppCache appCache = new AppCacheReader().load(path);
        App app = appCache.getById(5);

        assertThat(app.appId()).isEqualTo(5);
        assertThat(app.size()).isEqualTo(79);
        assertThat(app.infoState()).isEqualTo(1);
        assertThat(app.lastUpdated()).isEqualTo(1617940879);
        assertThat(app.picsToken()).isEqualTo(0);
        assertThat(byteArrayToHex(app.sha1())).isEqualTo("87fa436785800db490496ddc7db481ee518b8235");
        assertThat(app.changeNumber()).isEqualTo(13224007);

        VdfObject object = app.vdfObject();
        assertThat(object.getName()).isEqualTo("appinfo");
        assertThat(object.getValues().get(0)).isEqualTo(new VdfInteger("appid", 5));
        assertThat(object.getValues().get(1)).isEqualTo(new VdfInteger("public_only", 1));
    }

    @Test
    public void testAppId5_appinfo() {
        Path path = Resources.getResource("appinfo.vdf");
        AppCache appCache = new AppCacheReader().load(path);
        App app = appCache.getById(5);

        assertThat(app.appId()).isEqualTo(5);
        assertThat(app.size()).isEqualTo(85);
        assertThat(app.infoState()).isEqualTo(1);
        assertThat(app.lastUpdated()).isEqualTo(1594076880);
        assertThat(app.picsToken()).isEqualTo(0);
        assertThat(byteArrayToHex(app.sha1())).isEqualTo("87fa436785800db490496ddc7db481ee518b8235");
        assertThat(app.changeNumber()).isEqualTo(22534048);

        VdfObject object = app.vdfObject();
        assertThat(object.getName()).isEqualTo("appinfo");
        assertThat(object.getValues().get(0)).isEqualTo(new VdfInteger("appid", 5));
        assertThat(object.getValues().get(1)).isEqualTo(new VdfInteger("public_only", 1));
    }

    @Test
    public void testAppId5_magic28() {
        Path path = Resources.getResource("appinfo-magic-28.vdf");
        AppCache appCache = new AppCacheReader().load(path);
        App app = appCache.getById(5);

        assertThat(app.appId()).isEqualTo(5);
        assertThat(app.size()).isEqualTo(99);
        assertThat(app.infoState()).isEqualTo(1);
        assertThat(app.lastUpdated()).isEqualTo(1660750659);
        assertThat(app.picsToken()).isEqualTo(0);
        assertThat(byteArrayToHex(app.sha1())).isEqualTo("87fa436785800db490496ddc7db481ee518b8235");
        assertThat(app.changeNumber()).isEqualTo(17072555);

        VdfObject object = app.vdfObject();
        assertThat(object.getName()).isEqualTo("appinfo");
        assertThat(object.getValues().get(0)).isEqualTo(new VdfInteger("appid", 5));
        assertThat(object.getValues().get(1)).isEqualTo(new VdfInteger("public_only", 1));
    }

    private static String byteArrayToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
