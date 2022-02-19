package com.selesse.steam;

import com.selesse.os.Resources;
import com.selesse.steam.appcache.*;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.util.Formatter;

import static org.assertj.core.api.Assertions.assertThat;

public class AppCacheReaderTest {
    private AppCache appCache;

    @Before
    public void setup() {
        Path path = Resources.getResource("appinfo.vdf");
        appCache = new AppCacheReader().load(path);
    }

    @Test
    public void testCanParseAppCache() {
        App app = appCache.getById(5);
        assertThat(app.getAppId()).isEqualTo(5);
        assertThat(app.getSize()).isEqualTo(79);
        assertThat(app.getInfoState()).isEqualTo(1);
        assertThat(app.getLastUpdated()).isEqualTo(1617940879);
        assertThat(app.getPicsToken()).isEqualTo(0);
        assertThat(byteArrayToHex(app.getSha1())).isEqualTo("87fa436785800db490496ddc7db481ee518b8235");
        assertThat(app.getChangeNumber()).isEqualTo(13224007);

        VdfObject object = app.getVdfObject();

        assertThat(object.getName()).isEqualTo("appinfo");
        assertThat(object.getValues().get(0)).isEqualTo(new VdfInteger("appid", 5));
        assertThat(object.getValues().get(1)).isEqualTo(new VdfInteger("public_only", 1));
    }

    @Test
    public void testCanParseRightNumberOfApps() {
        assertThat(appCache.size()).isEqualTo(1947);
    }

    @Test
    public void testCanParseHollowKnight() {
        App hollowKnight = appCache.getById(TestGames.HOLLOW_KNIGHT.getGameId());

        VdfObject vdfObject = hollowKnight.getVdfObject();

        VdfObject common = (VdfObject) vdfObject.getValues().get(1);
        assertThat(common.getName()).isEqualTo("common");
        VdfString vdfString = (VdfString) common.getValues().get(0);
        assertThat(vdfString).isEqualTo(new VdfString("name", "Hollow Knight"));
    }

    private static String byteArrayToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}