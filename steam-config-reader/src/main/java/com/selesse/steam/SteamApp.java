package com.selesse.steam;

import com.google.common.base.Splitter;
import com.selesse.os.OperatingSystems;
import com.selesse.steam.games.SteamInstallationPaths;
import com.selesse.steam.games.UserFileSystemPath;
import com.selesse.steam.games.saves.SaveFile;
import com.selesse.steam.registry.SteamOperatingSystem;
import com.selesse.steam.registry.implementation.RegistryObject;
import com.selesse.steam.registry.implementation.RegistryString;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class SteamApp {
    private final RegistryObject registryObject;
    private final SteamInstall install;

    public SteamApp(RegistryObject registryObject, SteamInstall install) {
        this.registryObject = registryObject;
        this.install = install;
    }

    public RegistryObject getRegistryObject() {
        return registryObject;
    }

    /** The installation this app was read out of, and whose account its save paths resolve against. */
    public SteamInstall getInstall() {
        return install;
    }

    public long getId() {
        RegistryString objectValueAsString = registryObject.getObjectValueAsString("common/gameid");
        return Long.parseLong(objectValueAsString.getValue());
    }

    public String getName() {
        RegistryString objectValueAsString = registryObject.getObjectValueAsString("common/name");
        return objectValueAsString.getValue();
    }

    public AppType getType() {
        RegistryString objectValueAsString = registryObject.getObjectValueAsString("common/type");
        return AppType.fromString(objectValueAsString);
    }

    public String getInstallationDirectory() {
        return registryObject.getObjectValueAsString("config/installdir").getValue();
    }

    public String getInstallationDirectory(OperatingSystems.OperatingSystem os) {
        return SteamInstallationPaths.get(os) + "/" + getInstallationDirectory();
    }

    /**
     * Which platform's build Steam actually has on disk, according to the depots it installed.
     *
     * <p>Only depots declaring a single platform vote. A depot listing several (e.g. {@code
     * "windows,linux"}) is shared content serving every build, and one with no {@code oslist} at all
     * is platform-agnostic - neither says anything about what is running, so both abstain and the
     * answer is {@link InstalledBuild#UNKNOWN}.
     */
    public InstalledBuild getInstalledBuild() {
        List<String> installedDepotIds = install.registry().getInstalledDepotIds(getId());
        if (installedDepotIds.isEmpty()) {
            return InstalledBuild.UNKNOWN;
        }
        // Matched whole, so a shared "windows,linux" depot equals neither and abstains on its own.
        List<String> declaredPlatforms = installedDepotIds.stream()
                .map(depotId -> registryObject.findString("depots/" + depotId + "/config/oslist"))
                .flatMap(Optional::stream)
                .map(RegistryString::getValue)
                .toList();
        if (declaredPlatforms.contains("linux")) {
            return InstalledBuild.LINUX;
        }
        if (declaredPlatforms.contains("windows")) {
            return InstalledBuild.WINDOWS;
        }
        return InstalledBuild.UNKNOWN;
    }

    /** What {@link #getInstalledBuild} concluded, where {@code UNKNOWN} means "no usable signal". */
    public enum InstalledBuild {
        WINDOWS,
        LINUX,
        UNKNOWN
    }

    public List<OperatingSystems.OperatingSystem> getSupportedOperatingSystems() {
        RegistryString oslist = registryObject.findString("common/oslist").orElse(null);
        if (oslist == null) {
            return List.of(OperatingSystems.OperatingSystem.WINDOWS);
        }
        List<String> oses = Splitter.on(",").splitToList(oslist.getValue());
        return oses.stream()
                .flatMap(x -> SteamOperatingSystem.tryFromString(x).stream())
                .map(SteamOperatingSystem::toOperatingSystem)
                .toList();
    }

    public boolean supports(OperatingSystems.OperatingSystem operatingSystem) {
        return getSupportedOperatingSystems().contains(operatingSystem);
    }

    /**
     * Where this app's saves live when running on {@code os}, or empty if it has none there.
     */
    public List<UserFileSystemPath> getSavePaths(OperatingSystems.OperatingSystem os) {
        // ufs entries and rootoverrides only ever describe windows/macos/linux, so SteamOS reads
        // as Linux from here down. Windows is never gated on declared support: an app with no
        // oslist is treated as Windows-only, and Windows-rooted ufs entries are the fallback
        // shape even for apps that don't list Windows.
        OperatingSystems.OperatingSystem target = os.family().canonicalOs();
        if (target != OperatingSystems.OperatingSystem.WINDOWS && !supports(target)) {
            return List.of();
        }
        return new SaveFile(this).savePathsFor(target);
    }

    /** Whether {@link #getSavePaths} resolves to anything for {@code os}. */
    public boolean hasSavePathsFor(OperatingSystems.OperatingSystem os) {
        try {
            return !getSavePaths(os).isEmpty();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean hasAnySavePaths() {
        return Stream.of(
                        OperatingSystems.OperatingSystem.WINDOWS,
                        OperatingSystems.OperatingSystem.MAC,
                        OperatingSystems.OperatingSystem.LINUX)
                .anyMatch(this::hasSavePathsFor);
    }

    public boolean hasUserFileSystem() {
        return registryObject.pathExists("ufs/savefiles");
    }

    public boolean isGame() {
        return getType() == AppType.GAME;
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", getName(), getId());
    }
}
