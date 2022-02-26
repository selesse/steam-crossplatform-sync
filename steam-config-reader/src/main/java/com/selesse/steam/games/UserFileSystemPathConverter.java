package com.selesse.steam.games;

import com.selesse.steam.SteamApp;
import com.selesse.steam.games.saves.PathTransformation;
import com.selesse.steam.games.saves.RootOverrideObject;
import com.selesse.steam.registry.SteamOperatingSystem;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryString;
import java.util.List;

public class UserFileSystemPathConverter {
    public static List<UserFileSystemPath> convert(
            List<UserFileSystemPath> saveFileObjects, RootOverrideObject overrides, SteamApp steamApp) {
        return saveFileObjects.stream()
                .map(model -> {
                    String rootToUseInstead = SteamPathConverter.convert(overrides.getUseInstead());
                    if (rootToUseInstead.equals("gameinstall")) {
                        rootToUseInstead = steamApp.getInstallationDirectory(overrides.getOs());
                    }
                    String path = model.getRawPath();
                    if (overrides.hasPathTransforms()) {
                        var transformations = overrides.getPathTransformations();
                        for (PathTransformation transformation : transformations) {
                            path = path.replace(transformation.find(), transformation.replace());
                        }
                    }
                    if (overrides.hasPathAdditions()) {
                        if (overrides.getPathAddition().endsWith("/")) {
                            path = overrides.getPathAddition() + path;
                        } else {
                            path = overrides.getPathAddition() + "/" + path;
                        }
                    }

                    return new UserFileSystemPath(
                            rootToUseInstead, path, model.getPattern(), model.isRecursive(), model.getPlatform());
                })
                .toList();
    }

    public static List<RootOverrideObject> convertMacToLinux(List<RootOverrideObject> overrides) {
        return overrides.stream()
                .map(override -> {
                    RegistryObject registryObject = new RegistryObject();
                    registryObject.put("root", new RegistryString("root", override.getRoot()));
                    registryObject.put("os", new RegistryString("os", SteamOperatingSystem.LINUX.steamValue()));
                    registryObject.put("oscompare", new RegistryString("oscompare", override.getOsCompare()));
                    registryObject.put("useinstead", new RegistryString("useinstead", override.getUseInstead()));
                    return new RootOverrideObject(registryObject);
                })
                .toList();
    }
}
