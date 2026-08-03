package com.selesse.steam.games.saves;

import com.selesse.os.OperatingSystems.OperatingSystem;
import com.selesse.os.Resources;
import com.selesse.steam.SteamApp;
import com.selesse.steam.games.UserFileSystemPath;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import tools.jackson.databind.ObjectMapper;

public class BypassRegistrySaveFile extends SaveFile {
    private final SaveFileOverrides saveFileOverrides;

    public BypassRegistrySaveFile(SteamApp steamApp) {
        super(steamApp);

        try {
            saveFileOverrides = getSaveFileOverrides();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SaveFileOverrides getSaveFileOverrides() throws IOException {
        var objectMapper = new ObjectMapper();
        InputStream inputStream = Resources.getJarResource("save-location-overrides.json");
        return objectMapper.readValue(inputStream, SaveFileOverrides.class);
    }

    /** Whether this app has a hand-maintained override, and so bypasses the ufs registry entirely. */
    public boolean applies() {
        return saveFileOverrides.overrides().stream()
                .anyMatch(x -> steamApp.getName().equals(x.game()));
    }

    @Override
    public List<UserFileSystemPath> savePathsFor(OperatingSystem os) {
        SaveFileOverride override = getOverride();
        String path =
                switch (os) {
                    case WINDOWS -> override.windows();
                    case MAC -> override.mac();
                    case LINUX, STEAM_OS -> override.linux();
                };
        return List.of(new UserFileSystemPath(path));
    }

    private SaveFileOverride getOverride() {
        return saveFileOverrides.overrides().stream()
                .filter(x -> x.game().equals(steamApp.getName()))
                .findFirst()
                .orElseThrow();
    }
}
