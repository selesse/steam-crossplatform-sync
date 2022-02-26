package com.selesse.steam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.games.UserFileSystemPath;
import com.selesse.steam.games.saves.SaveFile;
import com.selesse.steam.games.saves.SaveFilesFactory;
import com.selesse.steam.registry.RegistryPrettyPrint;
import java.io.IOException;
import java.util.Collection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

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
            System.out.println(
                    RegistryPrettyPrint.prettyPrint(steamApp.getRegistryStore().getObjectValueAsObject("ufs")));
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
            var windowsInstallationPaths = steamApp.getWindowsInstallationPaths().stream()
                    .map(UserFileSystemPath::getSymbolPath)
                    .toList();
            assertThat(windowsInstallationPaths).isEqualTo(gameTestCase.windowsPath());
            var windowsSavePaths = saveFile.getWindowsSavePaths().stream()
                    .map(UserFileSystemPath::getSymbolPath)
                    .toList();
            assertThat(windowsSavePaths).isEqualTo(gameTestCase.windowsPath());
        }
        if (gameTestCase.mac() != null) {
            var macInstallationPaths = steamApp.getMacInstallationPaths().stream()
                    .map(UserFileSystemPath::getSymbolPath)
                    .toList();
            assertThat(macInstallationPaths).isEqualTo(gameTestCase.macPath());
            var macSavePaths = saveFile.getMacSavePaths().stream()
                    .map(UserFileSystemPath::getSymbolPath)
                    .toList();
            assertThat(macSavePaths).isEqualTo(gameTestCase.macPath());
        } else if (steamApp.getSupportedOperatingSystems().contains(OperatingSystems.OperatingSystem.MAC)) {
            fail(steamApp.getName() + " supports OS X but no path was provided. " + "Add \""
                    + steamApp.getMacInstallationPath() + "\" to the file.");
        }
        if (gameTestCase.linux() != null) {
            var linuxInstallationPaths = steamApp.getLinuxInstallationPaths().stream()
                    .map(UserFileSystemPath::getSymbolPath)
                    .toList();
            assertThat(linuxInstallationPaths).isEqualTo(gameTestCase.linuxPath());
            var linuxSavePaths = saveFile.getLinuxSavePaths().stream()
                    .map(UserFileSystemPath::getSymbolPath)
                    .toList();
            assertThat(linuxSavePaths).isEqualTo(gameTestCase.linuxPath());
        } else if (steamApp.getSupportedOperatingSystems().contains(OperatingSystems.OperatingSystem.LINUX)) {
            fail(steamApp.getName() + " supports Linux but no path was provided. " + "Add \""
                    + steamApp.getLinuxInstallationPath() + "\" to the file.");
        }
    }

    @Parameterized.Parameters(name = "#{0}")
    public static Collection<GameTestCase> data() throws IOException {
        TestAppCache.setup();
        return GameTestCase.load();
    }
}
