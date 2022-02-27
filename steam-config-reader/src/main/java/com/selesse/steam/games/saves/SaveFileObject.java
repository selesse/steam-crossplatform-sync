package com.selesse.steam.games.saves;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.SteamApp;
import com.selesse.steam.registry.implementation.RegistryObject;

public class SaveFileObject {
    private final SteamApp steamApp;
    private final RegistryObject object;

    public SaveFileObject(SteamApp steamApp, RegistryObject object) {
        this.steamApp = steamApp;
        this.object = object;
    }

    public String getRoot(OperatingSystems.OperatingSystem os) {
        String root = object.getObjectValueAsString("root").getValue();
        if (root.equals("gameinstall")) {
            return steamApp.getInstallationDirectory(os);
        }
        return root;
    }

    public String getPath() {
        return object.getObjectValueAsString("path").getValue();
    }

    public boolean hasPattern() {
        return object.pathExists("pattern");
    }

    public String getPattern() {
        return object.getObjectValueAsString("pattern").getValue();
    }

    public boolean hasRecursive() {
        return object.pathExists("recursive");
    }

    public boolean isRecursive() {
        return object.getObjectValueAsString("recursive").getValue().equals("1");
    }

    public boolean hasPlatform() {
        return object.pathExists("platforms");
    }

    public String getPlatform() {
        return object.getObjectValueAsString("platforms/1").getValue();
    }
}
