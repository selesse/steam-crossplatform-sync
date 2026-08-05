package com.selesse.os;

import java.nio.file.Files;
import java.nio.file.Path;

public class OperatingSystems {
    public enum OperatingSystem {
        WINDOWS,
        MAC,
        LINUX,
        STEAM_OS;

        /** Prefer switching on this: SteamOS differs from Linux only in the overlay process name. */
        public OperatingSystemFamily family() {
            return switch (this) {
                case WINDOWS -> OperatingSystemFamily.WINDOWS;
                case MAC -> OperatingSystemFamily.MAC;
                case LINUX, STEAM_OS -> OperatingSystemFamily.LINUX;
            };
        }
    }

    public enum OperatingSystemFamily {
        WINDOWS(OperatingSystem.WINDOWS),
        MAC(OperatingSystem.MAC),
        LINUX(OperatingSystem.LINUX);

        private final OperatingSystem canonicalOs;

        OperatingSystemFamily(OperatingSystem canonicalOs) {
            this.canonicalOs = canonicalOs;
        }

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
