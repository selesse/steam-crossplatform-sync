package com.selesse.steam.registry;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.selesse.processes.ProcessRunner;
import com.selesse.steam.registry.implementation.RegistryString;
import com.selesse.steam.steamcmd.games.SteamGameMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class WindowsSteamRegistry extends SteamRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(WindowsSteamRegistry.class);
    private static final String REGISTRY_COMMAND_TO_GET_APP_ID =
            "reg query HKEY_CURRENT_USER\\Software\\Valve\\Steam /v RunningAppId";
    private static final Pattern dwordPattern = Pattern.compile("([a-zA-Z0-9]+)\\s+REG_DWORD\\s+0x(.*)");

    @Override
    public long getCurrentlyRunningAppId() {
        String registryOutput =
                new ProcessRunner(Splitter.on(" ").splitToList(REGISTRY_COMMAND_TO_GET_APP_ID)).runAndGetOutput();
        return currentlyRunningAppIdBasedOnRegistryOutput(registryOutput);
    }

    @Override
    public List<Long> getInstalledAppIds() {
        return getGameMetadata().stream()
                .filter(SteamGameMetadata::isInstalled)
                .map(SteamGameMetadata::getGameId)
                .collect(Collectors.toList());
    }

    @Override
    public SteamGameMetadata getGameMetadata(Long gameId) {
        return getMetadataFromGame("HKEY_CURRENT_USER\\Software\\Valve\\Steam\\apps\\" + gameId, gameId);
    }

    @Override
    public List<SteamGameMetadata> getGameMetadata() {
        List<SteamGameMetadata> steamGames = Lists.newArrayList();

        ProcessRunner processRunner =
                new ProcessRunner("reg", "query", "HKEY_CURRENT_USER\\Software\\Valve\\Steam\\apps");
        List<String> apps = Splitter.on("\n").omitEmptyStrings().splitToList(processRunner.runAndGetOutput());
        for (String registryAppKey : apps) {
            List<String> registryQuery = Splitter.on("\\").splitToList(registryAppKey);
            long gameId = Long.parseLong(registryQuery.get(registryQuery.size() - 1));

            steamGames.add(getMetadataFromGame(registryAppKey, gameId));
        }
        return steamGames;
    }

    private SteamGameMetadata getMetadataFromGame(String app, long gameId) {
        ProcessRunner subProcessRunner = new ProcessRunner("reg", "query", app);
        String appRegistryOutput = subProcessRunner.runAndGetOutput();
        List<String> registryLines = Splitter.on("\n").splitToList(appRegistryOutput);
        List<RegistryString> registryStrings = registryLines.stream()
                .filter(this::isKeyValueRegistryLine)
                .map(String::trim)
                .map(WindowsSteamRegistry::extractKeyValue)
                .collect(Collectors.toList());
        String name = registryStrings.stream()
                .filter(x -> x.getName().equalsIgnoreCase("name"))
                .map(RegistryString::getValue).findFirst().orElse("");
        boolean installed = registryStrings.stream()
                .filter(x -> x.getName().equalsIgnoreCase("installed"))
                .map(x -> x.getValue().equals("1")).findFirst().orElse(false);

        return new SteamGameMetadata(gameId, name, installed);
    }

    private boolean isKeyValueRegistryLine(String line) {
        return dwordPattern.matcher(line).find();
    }

    private static RegistryString extractKeyValue(String line) {
        Matcher matcher = dwordPattern.matcher(line);
        matcher.find();
        String key = matcher.group(1);
        String value = matcher.group(2);
        return new RegistryString(key, value);
    }

    private static long currentlyRunningAppIdBasedOnRegistryOutput(String registryOutput) {
        Pattern regex = Pattern.compile("RunningAppId\\s+REG_DWORD\\s+0x(.*)");
        Matcher matcher = regex.matcher(registryOutput);
        if (matcher.find()) {
            String appHexId = matcher.group(1);
            long runningAppId = Long.parseLong(appHexId, 16);
            LOGGER.debug("Currently running Steam app ID is {}", runningAppId);
            return runningAppId;
        }
        throw new RuntimeException("Could not parse output of registry");
    }
}
