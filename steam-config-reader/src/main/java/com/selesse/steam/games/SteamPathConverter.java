package com.selesse.steam.games;

import java.util.function.Function;

class SteamPathConverter {
    private static final SteamPathConverter SYSTEM = new SteamPathConverter(System::getenv);

    private final Function<String, String> env;

    /**
     * @param env looks up an environment variable, returning {@code null} when it's unset
     */
    SteamPathConverter(Function<String, String> env) {
        this.env = env;
    }

    public static String convert(String path) {
        return SYSTEM.resolve(path);
    }

    String resolve(String path) {
        // https://partner.steamgames.com/doc/features/cloud
        return path.replace("WinMyDocuments", "%USERPROFILE%/Documents")
                .replace("WinAppDataLocal", "%USERPROFILE%/AppData/Local")
                .replace("WinAppDataLocalLow", "%USERPROFILE%/AppData/LocalLow")
                .replace("WinAppDataRoaming", "%USERPROFILE%/AppData/Roaming")
                .replace("WinSavedGames", "%USERPROFILE%/Saved Games")
                .replace("MacHome", "~")
                .replace("MacAppSupport", "~/Library/Application Support")
                .replace("MacDocuments", "~/Documents")
                .replace("LinuxHome", "~")
                .replace("LinuxXdgDataHome", envOrDefault("XDG_DATA_HOME", "~/.local/share"))
                .replace("LinuxXdgConfigHome", envOrDefault("XDG_CONFIG_HOME", "~/.config"));
    }

    private String envOrDefault(String name, String defaultValue) {
        String value = env.apply(name);
        return value != null ? value : defaultValue;
    }
}
