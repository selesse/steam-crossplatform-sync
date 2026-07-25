package com.selesse.steam.games;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class AppManifestInstalledGameFinderTest {
    @Test
    public void isFullyInstalledMatchesTheExactFullyInstalledFlag() {
        assertThat(AppManifestInstalledGameFinder.isFullyInstalled("4")).isTrue();
    }

    @Test
    public void isFullyInstalledMatchesFullyInstalledCombinedWithOtherBits() {
        // 6 = Fully Installed (4) | Update Required (2)
        assertThat(AppManifestInstalledGameFinder.isFullyInstalled("6")).isTrue();
        // 38 = Fully Installed (4) | Update Required (2) | 32
        assertThat(AppManifestInstalledGameFinder.isFullyInstalled("38")).isTrue();
        // 22 = Fully Installed (4) | Update Required (2) | 16
        assertThat(AppManifestInstalledGameFinder.isFullyInstalled("22")).isTrue();
    }

    @Test
    public void isFullyInstalledIsFalseWhenTheFullyInstalledBitIsNotSet() {
        assertThat(AppManifestInstalledGameFinder.isFullyInstalled("2")).isFalse();
        assertThat(AppManifestInstalledGameFinder.isFullyInstalled("0")).isFalse();
    }
}
