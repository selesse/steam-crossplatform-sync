package com.selesse.steam;

import com.google.common.annotations.VisibleForTesting;
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

    // Memoized: reading it parses an app manifest, and the lookups below ask several times per app.
    private List<String> installedDepotIds;

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
     * Which platform's build Steam has on disk. Only depots naming a single platform vote: one
     * tagged {@code "windows,linux"} is shared content and one with no {@code oslist} is
     * platform-agnostic, so neither says anything about which build runs.
     */
    public InstalledBuild getInstalledBuild() {
        List<String> installedDepotIds = getInstalledDepotIds();
        if (installedDepotIds.isEmpty()) {
            return InstalledBuild.UNKNOWN;
        }
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
     * Whether this app runs on {@code os} here, which is not what its store page advertises: most of
     * a Deck's library is Windows-only by that measure and runs through Proton anyway. Steam does
     * not install a game it cannot run, so {@code oslist} only decides for apps that aren't
     * installed.
     */
    public boolean runsOn(OperatingSystems.OperatingSystem os) {
        return runsOn(os, OperatingSystems.get());
    }

    @VisibleForTesting
    boolean runsOn(OperatingSystems.OperatingSystem os, OperatingSystems.OperatingSystem runningOn) {
        // Being installed proves it runs on *this* machine, so it settles nothing when asked from a
        // Mac about Linux.
        boolean askingAboutThisMachine = os.family() == runningOn.family();
        if (os.family() == OperatingSystems.OperatingSystemFamily.LINUX
                && askingAboutThisMachine
                && isInstalledHere()) {
            return true;
        }
        return supports(os);
    }

    public boolean isInstalledHere() {
        return !getInstalledDepotIds().isEmpty();
    }

    private List<String> getInstalledDepotIds() {
        if (installedDepotIds == null) {
            installedDepotIds = install.registry().getInstalledDepotIds(getId());
        }
        return installedDepotIds;
    }

    /**
     * Where this app's saves live when running on {@code os}, or empty if it has none there.
     */
    public List<UserFileSystemPath> getSavePaths(OperatingSystems.OperatingSystem os) {
        // ufs entries and rootoverrides only ever describe windows/macos/linux, so SteamOS reads
        // as Linux from here down. Windows is never gated on whether the app runs there: an app with
        // no oslist is treated as Windows-only, and Windows-rooted ufs entries are the fallback
        // shape even for apps that don't list Windows.
        OperatingSystems.OperatingSystem target = os.family().canonicalOs();
        if (target != OperatingSystems.OperatingSystem.WINDOWS && !runsOn(target)) {
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
