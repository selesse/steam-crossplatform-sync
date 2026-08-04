package com.selesse.os;

import java.nio.file.Files;
import java.nio.file.Path;

public class OperatingSystems {
    public enum OperatingSystem {
        WINDOWS,
        MAC,
        LINUX,
        STEAM_OS;

        /**
         * The platform behaviour this OS follows. SteamOS is Linux for everything except the Steam
         * overlay process name, so switch on this rather than on the OS itself unless that one
         * difference is what you're after.
         */
        public OperatingSystemFamily family() {
            return switch (this) {
                case WINDOWS -> OperatingSystemFamily.WINDOWS;
                case MAC -> OperatingSystemFamily.MAC;
                case LINUX, STEAM_OS -> OperatingSystemFamily.LINUX;
            };
        }
    }

    /**
     * Deliberately has no {@code STEAM_OS}, so a switch over a family has nothing to get wrong -
     * where nine separate {@code case LINUX, STEAM_OS} arms each had to pair the two correctly.
     * {@link OperatingSystem#family()} is now the only place SteamOS is stated to be Linux.
     */
    public enum OperatingSystemFamily {
        WINDOWS(OperatingSystem.WINDOWS),
        MAC(OperatingSystem.MAC),
        LINUX(OperatingSystem.LINUX);

        private final OperatingSystem canonicalOs;

        OperatingSystemFamily(OperatingSystem canonicalOs) {
            this.canonicalOs = canonicalOs;
        }

        /** The OS standing in for this whole family, for code that still needs an OS downstream. */
        public OperatingSystem canonicalOs() {
            return canonicalOs;
        }
    }

    public static OperatingSystem get() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            return OperatingSystem.WINDOWS;
        } else if (osName.contains("darwin") || osName.contains("mac os")) {
            return OperatingSystem.MAC;
        } else if (osName.contains("linux")) {
            if (Files.exists(Path.of("/etc/steamos-release"))) {
                return OperatingSystem.STEAM_OS;
            }
            return OperatingSystem.LINUX;
        }
        throw new IllegalArgumentException("Unsupported OS: " + osName);
    }
}
