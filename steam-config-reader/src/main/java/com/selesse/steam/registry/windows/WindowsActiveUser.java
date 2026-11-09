package com.selesse.steam.registry.windows;

import com.selesse.processes.ProcessRunner;
import java.util.List;
import java.util.OptionalLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The 32-bit account id of whoever is signed in to Steam right now, from the Windows registry.
 * Steam writes 0 there when nobody is signed in.
 */
public class WindowsActiveUser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WindowsActiveUser.class);

    private static final List<String> COMMAND =
            List.of("reg", "query", "HKCU\\Software\\Valve\\Steam\\ActiveProcess", "/v", "ActiveUser");
    private static final Pattern ACTIVE_USER_PATTERN = Pattern.compile("ActiveUser\\s+REG_DWORD\\s+0x([0-9a-fA-F]+)");

    public static OptionalLong get() {
        try {
            return parse(new ProcessRunner(COMMAND).runAndGetOutput());
        } catch (RuntimeException e) {
            LOGGER.warn("Could not read Steam's active user from the registry", e);
            return OptionalLong.empty();
        }
    }

    static OptionalLong parse(String regQueryOutput) {
        Matcher matcher = ACTIVE_USER_PATTERN.matcher(regQueryOutput);
        if (!matcher.find()) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(Long.parseLong(matcher.group(1), 16));
    }
}
