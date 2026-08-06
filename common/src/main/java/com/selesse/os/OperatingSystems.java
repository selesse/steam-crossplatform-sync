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
        WINDOWS(OperatingSystem.WINDOWS, "Windows"),
        MAC(OperatingSystem.MAC, "Mac"),
        LINUX(OperatingSystem.LINUX, "Linux");

        private final OperatingSystem canonicalOs;
        private final String displayName;

        OperatingSystemFamily(OperatingSystem canonicalOs, String displayName) {
            this.canonicalOs = canonicalOs;
            this.displayName = displayName;
        }

        public OperatingSystem canonicalOs() {
            return canonicalOs;
        }

        /** How to name this family to a person, e.g. in {@code --print-games} output. */
        public String displayName() {
            return displayName;
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
