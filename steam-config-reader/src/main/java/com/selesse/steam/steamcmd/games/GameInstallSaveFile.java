package com.selesse.steam.steamcmd.games;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryStore;

import java.util.Optional;

public class GameInstallSaveFile implements SaveFile {
    private final RegistryStore store;
    private final RegistryStore gameRegistry;

    public GameInstallSaveFile(RegistryStore store, RegistryStore gameRegistry) {
        this.store = store;
        this.gameRegistry = gameRegistry;
    }

    @Override
    public boolean applies() {
        return gameRegistry.pathExists("config/installdir") &&
                store.pathExists("savefiles/0/root") &&
                hasGameInstallInProperties();
    }

    private boolean hasGameInstallInProperties() {
        return (store.pathExists("rootoverrides/0/useinstead") &&
                store.getObjectValueAsString("rootoverrides/0/useinstead").getValue().equals("gameinstall"))
                || store.getObjectValueAsString("savefiles/0/root").getValue().equals("gameinstall");
    }

    @Override
    public UserFileSystemPath getMacInfo() {
        RegistryObject macObject = null;
        RegistryObject rootOverrides = store.getObjectValueAsObject("rootoverrides");
        for (String key : rootOverrides.getKeys()) {
            RegistryObject osObject = rootOverrides.getObjectValueAsObject(key);
            if (osObject.getObjectValueAsString("os").getValue().equals("macos")) {
                macObject = osObject;
            }
        }
        RegistryObject macRegistryObject = Optional.ofNullable(macObject).orElseThrow();
        String root;
        String path;
        if (isSlayTheSpire()) {
            String macSpecial = store.getObjectValueAsString("rootoverrides/0/addpath").getValue();
            root = computeRoot(OperatingSystems.OperatingSystem.MAC) + "/" + macSpecial;
            path = getWindowsInfo().getPath();
        } else {
            if (macRegistryObject.pathExists("useinstead")) {
                String useInsteadValue = macRegistryObject.getObjectValueAsString("useinstead").getValue();
                if (useInsteadValue.equals("gameinstall")) {
                    root = computeRoot(OperatingSystems.OperatingSystem.MAC);
                } else {
                    root = useInsteadValue;
                }
            } else {
                root = macRegistryObject.getObjectValueAsString("root").getValue();
            }
            path = getWindowsInfo().getPath();
        }
        return new UserFileSystemPath(root, path);
    }

    @Override
    public UserFileSystemPath getWindowsInfo() {
        String root;
        String path;
        if (isSlayTheSpire()) {
            root = computeRoot(OperatingSystems.OperatingSystem.WINDOWS);
            path = store.getObjectValueAsString("savefiles/3/path").getValue();
        } else {
            root = store.getObjectValueAsString("savefiles/0/root").getValue();
            path = store.getObjectValueAsString("savefiles/0/path").getValue();
        }
        return new UserFileSystemPath(root, path);
    }

    private boolean isSlayTheSpire() {
        return gameRegistry.getObjectValueAsString("common/name").getValue().equals("Slay the Spire");
    }

    private String computeRoot(OperatingSystems.OperatingSystem os) {
        String installationDir = gameRegistry.getObjectValueAsString("config/installdir").getValue();
        return SteamInstallationPaths.get(os) + "/" + installationDir;
    }
}
