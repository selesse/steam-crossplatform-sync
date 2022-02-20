package com.selesse.steam.registry.windows;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistryDwordParser {
    public static Optional<Long> getValueFromOutput(String registryOutput, String key) {
        Pattern regex = Pattern.compile(Pattern.quote(key) + "\\s+REG_DWORD\\s+0x(.*)");
        Matcher matcher = regex.matcher(registryOutput);
        if (matcher.find()) {
            String appHexId = matcher.group(1);
            return Optional.of(Long.parseLong(appHexId, 16));
        }
        return Optional.empty();
    }
}
