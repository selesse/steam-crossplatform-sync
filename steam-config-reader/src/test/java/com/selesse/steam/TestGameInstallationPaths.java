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
import java.util.List;
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
                    RegistryPrettyPrint.prettyPrint(steamApp.getRegistryObject().getObjectValueAsObject("ufs")));
        }
    };

    public TestGameInstallationPaths(GameTestCase GameTestCase) {
        this.gameTestCase = GameTestCase;
    }

    @Test
    public void testGame() {
        steamApp = SteamAppLoader.findByName(TestAppCache.PATH, gameTestCase.name());
        SaveFile saveFile = SaveFilesFactory.determineSaveFile(steamApp);

        if (gameTestCase.windows() != null) {
            assertThat(symbolPaths(steamApp, OperatingSystems.OperatingSystem.WINDOWS))
                    .isEqualTo(gameTestCase.windowsPath());
            assertThat(symbolPaths(saveFile, OperatingSystems.OperatingSystem.WINDOWS))
                    .isEqualTo(gameTestCase.windowsPath());
        }
        if (gameTestCase.mac() != null) {
            assertThat(symbolPaths(steamApp, OperatingSystems.OperatingSystem.MAC))
                    .isEqualTo(gameTestCase.macPath());
            assertThat(symbolPaths(saveFile, OperatingSystems.OperatingSystem.MAC))
                    .isEqualTo(gameTestCase.macPath());
        } else if (steamApp.getSupportedOperatingSystems().contains(OperatingSystems.OperatingSystem.MAC)) {
            fail(steamApp.getName() + " supports OS X but no path was provided. " + "Add \""
                    + symbolPaths(steamApp, OperatingSystems.OperatingSystem.MAC) + "\" to the file.");
        }
        if (gameTestCase.linux() != null) {
            if (!gameTestCase.linux().contains("unsupported")) {
                assertThat(symbolPaths(steamApp, OperatingSystems.OperatingSystem.LINUX))
                        .isEqualTo(gameTestCase.linuxPath());
                assertThat(symbolPaths(saveFile, OperatingSystems.OperatingSystem.LINUX))
                        .isEqualTo(gameTestCase.linuxPath());
            }
        } else if (steamApp.getSupportedOperatingSystems().contains(OperatingSystems.OperatingSystem.LINUX)) {
            fail(steamApp.getName() + " supports Linux but no path was provided. " + "Add \""
                    + symbolPaths(steamApp, OperatingSystems.OperatingSystem.LINUX) + "\" to the file.");
        }
    }

    private List<String> symbolPaths(SteamApp steamApp, OperatingSystems.OperatingSystem os) {
        return steamApp.getSavePaths(os).stream()
                .map(UserFileSystemPath::getSymbolPath)
                .toList();
    }

    private List<String> symbolPaths(SaveFile saveFile, OperatingSystems.OperatingSystem os) {
        return saveFile.savePathsFor(os).stream()
                .map(UserFileSystemPath::getSymbolPath)
                .toList();
    }

    @Parameterized.Parameters(name = "#{0}")
    public static Collection<GameTestCase> data() throws IOException {
        return GameTestCase.load();
    }
}
