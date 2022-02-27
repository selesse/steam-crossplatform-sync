package com.selesse.steam.games.saves;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selesse.os.Resources;
import com.selesse.steam.SteamApp;
import com.selesse.steam.games.UserFileSystemPath;
import java.io.IOException;
import java.io.InputStream;

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

    @Override
    public boolean applies() {
        return saveFileOverrides.overrides().stream()
                .anyMatch(x -> steamApp.getName().equals(x.game()));
    }

    @Override
    public UserFileSystemPath getMacInfo() {
        return new UserFileSystemPath(getOverride().mac());
    }

    @Override
    public UserFileSystemPath getWindowsInfo() {
        return new UserFileSystemPath(getOverride().windows());
    }

    @Override
    public UserFileSystemPath getLinuxInfo() {
        return new UserFileSystemPath(getOverride().linux());
    }

    private SaveFileOverride getOverride() {
        return saveFileOverrides.overrides().stream()
                .filter(x -> x.game().equals(steamApp.getName()))
                .findFirst()
                .orElseThrow();
    }
}
