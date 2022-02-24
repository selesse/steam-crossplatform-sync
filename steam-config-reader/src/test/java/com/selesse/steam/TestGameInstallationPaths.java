package com.selesse.steam;


import com.selesse.os.OperatingSystems;
import com.selesse.steam.games.saves.SaveFile;
import com.selesse.steam.games.saves.SaveFilesFactory;
import com.selesse.steam.registry.RegistryPrettyPrint;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(Parameterized.class)
public class TestGameInstallationPaths {
    private final GameTestCase gameTestCase;
    private SteamApp steamApp;

    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            super.failed(e, description);

            System.out.println("Failed to test " + gameTestCase.name() + "'s installation paths");
            System.out.println(RegistryPrettyPrint.prettyPrint(steamApp.getRegistryStore().getObjectValueAsObject("ufs")));
        }
    };

    public TestGameInstallationPaths(GameTestCase GameTestCase) {
        this.gameTestCase = GameTestCase;
    }

    @Test
    public void testGame() {
        steamApp = SteamAppLoader.findByName(gameTestCase.name());
        SaveFile saveFile = SaveFilesFactory.determineSaveFile(steamApp);

        if (gameTestCase.windows() != null) {
            assertThat(steamApp.getWindowsInstallationPath()).isEqualTo(gameTestCase.windowsPath());
            assertThat(saveFile.getWindowsInfo().getSymbolPath()).isEqualTo(gameTestCase.windowsPath());
        }
        if (gameTestCase.mac() != null) {
            assertThat(steamApp.getMacInstallationPath()).isEqualTo(gameTestCase.macPath());
            assertThat(saveFile.getMacInfo().getSymbolPath()).isEqualTo(gameTestCase.macPath());
        } else if (steamApp.getSupportedOperatingSystems().contains(OperatingSystems.OperatingSystem.MAC)) {
            fail(steamApp.getName() + " supports OS X but no path was provided. " +
                    "Add \"" + steamApp.getMacInstallationPath() + "\" to the file.");
        }
        if (gameTestCase.linux() != null) {
            assertThat(steamApp.getLinuxInstallationPath()).isEqualTo(gameTestCase.linuxPath());
            assertThat(saveFile.getLinuxInfo().getSymbolPath()).isEqualTo(gameTestCase.linuxPath());
        } else if (steamApp.getSupportedOperatingSystems().contains(OperatingSystems.OperatingSystem.LINUX)) {
            fail(steamApp.getName() + " supports Linux but no path was provided. " +
                    "Add \"" + steamApp.getLinuxInstallationPath() + "\" to the file.");
        }
    }

    @Parameterized.Parameters(name = "#{0}")
    public static Collection<GameTestCase> data() throws IOException {
        TestAppCache.setup();
        return GameTestCase.load();
    }
}
