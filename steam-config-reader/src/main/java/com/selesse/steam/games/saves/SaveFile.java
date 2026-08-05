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
        var overrides = readRootOverrides(os);
        boolean hasExplicitLinuxOverride =
                overrides.stream().anyMatch(x -> x.getOs() == OperatingSystems.OperatingSystem.LINUX);

        if (os == OperatingSystems.OperatingSystem.LINUX) {
            Optional<List<UserFileSystemPath>> protonResolved = tryResolveViaProton(hasExplicitLinuxOverride);
            if (protonResolved.isPresent()) {
                return protonResolved.get();
            }
        }

        if (!overrides.isEmpty()) {
            if (os == OperatingSystems.OperatingSystem.LINUX && !hasExplicitLinuxOverride) {
                overrides = UserFileSystemPathConverter.convertMacToLinux(overrides);
            }
            return overrides.stream()
                    .filter(o -> o.getOs() == os)
                    .map(overrideObject ->
                            UserFileSystemPathConverter.convert(saveFileObjects, overrideObject, steamApp))
                    .flatMap(List::stream)
                    .toList();
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
     * The app's {@code rootoverrides}, or empty when it has none or when they don't apply. They only
     * ever redirect away from the Windows shape, so a Windows target never consults them.
     */
    private List<RootOverrideObject> readRootOverrides(OperatingSystems.OperatingSystem os) {
        if (os == OperatingSystems.OperatingSystem.WINDOWS || !ufs.pathExists("rootoverrides")) {
            return List.of();
        }
        return ufs.getObjectValueAsObject("rootoverrides").getKeys().stream()
                .map(key -> ufs.getObjectValueAsObject("rootoverrides/" + key))
                .map(RootOverrideObject::new)
                .toList();
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
     * <p>{@link SteamApp#getInstalledBuild} settles it whenever it can, and outranks an explicit
     * Linux {@code rootoverride}: an override says where the <em>native</em> build keeps its saves,
     * not which build is installed, so honouring it for a Proton install names a directory the game
     * never writes to.
     *
     * <p>Only when the depots abstain do the guesses below get a say, and then the override wins -
     * a location the developer declared beats one we inferred.
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
