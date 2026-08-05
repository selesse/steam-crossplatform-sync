package com.selesse.steam.games.saves;

import com.selesse.os.OperatingSystems;
import com.selesse.steam.SteamApp;
import com.selesse.steam.games.SteamInstallationPaths;
import com.selesse.steam.games.UserFileSystemPath;
import com.selesse.steam.games.UserFileSystemPathConverter;
import com.selesse.steam.registry.implementation.RegistryObject;
import java.util.List;
import java.util.Optional;

/**
 * An app's save locations, as described by the {@code ufs} block of its app cache entry.
 */
public class SaveFile {
    private final SteamApp steamApp;
    private final RegistryObject ufs;

    public SaveFile(SteamApp steamApp) {
        this.steamApp = steamApp;
        this.ufs = steamApp.getRegistryObject().getObjectValueAsObject("ufs");
    }

    /**
     * Where this app's saves live when running on {@code os}. Only windows/macos/linux are ever
     * passed in - SteamOS is normalized to Linux upstream, in {@link SteamApp#getSavePaths}.
     */
    public List<UserFileSystemPath> savePathsFor(OperatingSystems.OperatingSystem os) {
        var saveFileObjects = ufs.getObjectValueAsObject("savefiles").getKeys().stream()
                .map(key -> ufs.getObjectValueAsObject("savefiles/" + key))
                .map(registryObject -> new SaveFileObject(steamApp, registryObject))
                .map(saveFileObject -> UserFileSystemPath.fromSaveFile(
                        saveFileObject, os, steamApp.getInstall().accountId()))
                .toList();
        if (ufs.pathExists("rootoverrides") && os != OperatingSystems.OperatingSystem.WINDOWS) {
            var overrides = ufs.getObjectValueAsObject("rootoverrides").getKeys().stream()
                    .map(key -> ufs.getObjectValueAsObject("rootoverrides/" + key))
                    .map(RootOverrideObject::new)
                    .toList();
            boolean hasExplicitLinuxOverride =
                    overrides.stream().anyMatch(x -> x.getOs() == OperatingSystems.OperatingSystem.LINUX);
            if (os == OperatingSystems.OperatingSystem.LINUX) {
                Optional<List<UserFileSystemPath>> protonResolved = tryResolveViaProton(hasExplicitLinuxOverride);
                if (protonResolved.isPresent()) {
                    return protonResolved.get();
                }
                if (!hasExplicitLinuxOverride) {
                    overrides = UserFileSystemPathConverter.convertMacToLinux(overrides);
                }
            }
            return overrides.stream()
                    .filter(o -> o.getOs() == os)
                    .map(overrideObject ->
                            UserFileSystemPathConverter.convert(saveFileObjects, overrideObject, steamApp))
                    .flatMap(List::stream)
                    .toList();
        }
        if (os == OperatingSystems.OperatingSystem.LINUX) {
            Optional<List<UserFileSystemPath>> protonResolved = tryResolveViaProton(false);
            if (protonResolved.isPresent()) {
                return protonResolved.get();
            }
        }
        if (isNonWindowsButWeOnlyHaveWindowsSaveFiles(os, saveFileObjects)) {
            return saveFileObjects.stream().map(x -> x.convert(os)).toList();
        }
        if (saveFileObjects.stream().anyMatch(x -> x.getPlatform() != null)) {
            return saveFileObjects.stream().filter(x -> x.getPlatform() == os).toList();
        }
        return saveFileObjects;
    }

    /**
     * A Linux/SteamOS machine running this specific game under Proton has its saves in a Windows-shaped
     * user profile inside the compatdata prefix, not wherever the native-Linux ufs entries point. Returns
     * empty when Proton isn't in play here, or when the Windows-target resolution isn't purely
     * user-profile-rooted (e.g. a gameinstall-rooted save, which lives at the same path either way).
     */
    private Optional<List<UserFileSystemPath>> tryResolveViaProton(boolean hasExplicitLinuxOverride) {
        if (!isRunningUnderProton(hasExplicitLinuxOverride)) {
            return Optional.empty();
        }

        List<UserFileSystemPath> windowsSavePaths = savePathsFor(OperatingSystems.OperatingSystem.WINDOWS);
        boolean allWinRooted = !windowsSavePaths.isEmpty()
                && windowsSavePaths.stream().allMatch(p -> p.getRoot().startsWith("Win"));
        if (!allWinRooted) {
            return Optional.empty();
        }

        String protonPrefixRoot = SteamInstallationPaths.getProtonPrefixUserProfileRoot(steamApp.getId());
        return Optional.of(windowsSavePaths.stream()
                .map(p -> p.rerootForProton(protonPrefixRoot))
                .toList());
    }

    /**
     * Whether this Linux machine runs this game through Proton.
     *
     * <p>The depots Steam installed settle it outright when they name a single platform, and are
     * preferred over everything else: a Windows build on a Linux box can only run under Proton, and
     * a native Linux build cannot. That beats an explicit Linux {@code rootoverride}, which says
     * where the *native* build keeps its saves and not which build is installed - honouring it for a
     * Proton install points at a directory the game never writes.
     *
     * <p>When the depots abstain, fall back to the weaker signals. Both are known to be lossy: a
     * compatdata prefix outlives a switch back to a native build, and {@code common/oslist} is store
     * metadata that can omit a Linux build the game ships. An explicit Linux override is the
     * tie-breaker in that case, since a declared native location beats a guess.
     */
    private boolean isRunningUnderProton(boolean hasExplicitLinuxOverride) {
        return switch (steamApp.getInstalledBuild()) {
            case WINDOWS -> true;
            case LINUX -> false;
            case UNKNOWN ->
                !hasExplicitLinuxOverride
                        && (steamApp.getInstall().registry().hasActiveProtonPrefix(steamApp.getId())
                                || !steamApp.getSupportedOperatingSystems()
                                        .contains(OperatingSystems.OperatingSystem.LINUX));
        };
    }

    private boolean isNonWindowsButWeOnlyHaveWindowsSaveFiles(
            OperatingSystems.OperatingSystem os, List<UserFileSystemPath> saveFileObjects) {
        return steamApp.getSupportedOperatingSystems().size() > 1
                && onlyHasWindows(saveFileObjects)
                && os != OperatingSystems.OperatingSystem.WINDOWS;
    }

    private boolean onlyHasWindows(List<UserFileSystemPath> saveFileObjects) {
        return saveFileObjects.stream().allMatch(x -> x.getPlatform() == OperatingSystems.OperatingSystem.WINDOWS);
    }
}
