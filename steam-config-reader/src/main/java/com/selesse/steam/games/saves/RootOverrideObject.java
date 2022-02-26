package com.selesse.steam.games.saves;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.registry.SteamOperatingSystem;
import com.selesse.steam.registry.implementation.RegistryObject;
import java.util.List;

public class RootOverrideObject {
    private final RegistryObject registryObject;

    public RootOverrideObject(RegistryObject registryObject) {
        this.registryObject = registryObject;
    }

    public String getRoot() {
        return registryObject.getObjectValueAsString("root").getValue();
    }

    public OperatingSystems.OperatingSystem getOs() {
        String osValue = registryObject.getObjectValueAsString("os").getValue();
        return SteamOperatingSystem.fromString(osValue).toOperatingSystem();
    }

    public String getOsCompare() {
        return registryObject.getObjectValueAsString("oscompare").getValue();
    }

    public String getUseInstead() {
        return registryObject.getObjectValueAsString("useinstead").getValue();
    }

    public List<PathTransformation> getPathTransformations() {
        return registryObject.getObjectValueAsObject("pathtransforms").getKeys().stream()
                .map(x -> registryObject.getObjectValueAsObject("pathtransforms/" + x))
                .map(x -> new PathTransformation(
                        x.getObjectValueAsString("find").getValue(),
                        x.getObjectValueAsString("replace").getValue()))
                .toList();
    }

    public boolean hasPathTransforms() {
        return registryObject.pathExists("pathtransforms");
    }

    public boolean hasPathAdditions() {
        return registryObject.pathExists("addpath");
    }

    public String getPathAddition() {
        return registryObject.getObjectValueAsString("addpath").getValue();
    }
}
