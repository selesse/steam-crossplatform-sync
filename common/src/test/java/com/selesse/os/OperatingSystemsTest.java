package com.selesse.os;

import static org.assertj.core.api.Assertions.assertThat;

import com.selesse.os.OperatingSystems.OperatingSystem;
import com.selesse.os.OperatingSystems.OperatingSystemFamily;
import org.junit.Test;

// family() is the single place SteamOS is stated to be Linux - every switch that used to pair
// LINUX and STEAM_OS now goes through it, so getting it wrong is wrong everywhere at once.
public class OperatingSystemsTest {
    @Test
    public void steamOsBelongsToTheLinuxFamily() {
        assertThat(OperatingSystem.STEAM_OS.family()).isEqualTo(OperatingSystemFamily.LINUX);
        assertThat(OperatingSystem.LINUX.family()).isEqualTo(OperatingSystemFamily.LINUX);
    }

    @Test
    public void everyOtherOsIsItsOwnFamily() {
        assertThat(OperatingSystem.WINDOWS.family()).isEqualTo(OperatingSystemFamily.WINDOWS);
        assertThat(OperatingSystem.MAC.family()).isEqualTo(OperatingSystemFamily.MAC);
    }

    @Test
    public void steamOsNormalizesToLinuxThroughItsFamily() {
        assertThat(OperatingSystem.STEAM_OS.family().canonicalOs()).isEqualTo(OperatingSystem.LINUX);
    }

    @Test
    public void aCanonicalOsIsAlwaysInTheFamilyItCameFrom() {
        for (OperatingSystemFamily family : OperatingSystemFamily.values()) {
            assertThat(family.canonicalOs().family()).isEqualTo(family);
        }
    }

    @Test
    public void everyOsHasAFamily() {
        for (OperatingSystem os : OperatingSystem.values()) {
            assertThat(os.family()).isNotNull();
        }
    }
}
