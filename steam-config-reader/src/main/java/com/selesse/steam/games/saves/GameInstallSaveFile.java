package com.selesse.steam.games.saves;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.games.SteamInstallationPaths;
import com.selesse.steam.games.UserFileSystemPath;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryStore;

import java.util.Optional;

public class GameInstallSaveFile extends SaveFile {
    public GameInstallSaveFile(RegistryStore gameRegistry) {
        super(gameRegistry);
    }

    @Override
    public boolean applies() {
        return gameRegistry.pathExists("config/installdir") &&
                ufs.pathExists("savefiles/0/root") &&
                hasGameInstallInProperties();
    }

    private boolean hasGameInstallInProperties() {
        return (ufs.pathExists("rootoverrides/0/useinstead") &&
                ufs.getObjectValueAsString("rootoverrides/0/useinstead").getValue().equals("gameinstall"))
                || ufs.getObjectValueAsString("savefiles/0/root").getValue().equals("gameinstall");
    }

    @Override
    public UserFileSystemPath getMacInfo() {
        RegistryObject macObject = null;
        RegistryObject rootOverrides = ufs.getObjectValueAsObject("rootoverrides");
        for (String key : rootOverrides.getKeys()) {
            RegistryObject osObject = rootOverrides.getObjectValueAsObject(key);
            if (osObject.getObjectValueAsString("os").getValue().equals("macos")) {
                macObject = osObject;
            }
        }
        String root;
        String path;
        if (isSlayTheSpire()) {
            String macSpecial = ufs.getObjectValueAsString("rootoverrides/0/addpath").getValue();
            root = computeRoot(OperatingSystems.OperatingSystem.MAC) + "/" + macSpecial;
        } else {
            RegistryObject macRegistryObject = Optional.ofNullable(macObject).orElseThrow();
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
        }
        path = getWindowsInfo().getPath();
        return new UserFileSystemPath(root, path);
    }

    @Override
    public UserFileSystemPath getWindowsInfo() {
        String root;
        String path;
        if (isSlayTheSpire()) {
            root = computeRoot(OperatingSystems.OperatingSystem.WINDOWS);
            path = ufs.getObjectValueAsString("savefiles/3/path").getValue();
        } else {
            root = ufs.getObjectValueAsString("savefiles/0/root").getValue();
            path = ufs.getObjectValueAsString("savefiles/0/path").getValue();
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
