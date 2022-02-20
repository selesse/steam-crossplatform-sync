package com.selesse.steam.steamcmd;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.selesse.os.OperatingSystems;
import com.selesse.processes.ProcessRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

class PrintAppInfoExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrintAppInfoExecutor.class);

    public List<String> runPrintAppInfoProcess(Long appId) {
        ProcessRunner processRunner = new ProcessRunner(
                "steamcmd", "+login", "anonymous", "+app_info_print", appId.toString(), "+quit"
        );
        String output = processRunner.runAndGetOutput();
        List<String> lines = Splitter.on("\n").splitToList(output);
        if (lines.contains("No app info for AppID " + appId + " found, requesting...")) {
            if (OperatingSystems.get() != OperatingSystems.OperatingSystem.WINDOWS) {
                LOGGER.info("App {} wasn't found, using interactive app info print", appId);
                return new AppInfoPrintInteractive().runPrintAppInfoProcess(appId);
            }
        }
        int firstLine = lines.indexOf(String.format("\"%d\"", appId));
        int lastLine = lines.lastIndexOf("}") + 1;
        return lines.subList(firstLine, lastLine);
    }

    public Map<Long, List<String>> runPrintAppInfoProcesses(List<Long> appIds) {
        List<String> commands = Lists.newArrayList("steamcmd", "+login", "anonymous");
        for (Long appId : appIds) {
            commands.add("+app_info_print");
            commands.add(appId.toString());
        }
        commands.add("+quit");
        ProcessRunner processRunner = new ProcessRunner(commands.toArray(new String[0]));
        String output = processRunner.runAndGetOutput();

        List<String> outputLines = Splitter.on("\n").splitToList(output);

        Map<Long, List<String>> outputPerAppId = Maps.newHashMap();

        for (Long appId : appIds) {
            for (int i = 0; i < outputLines.size(); i++) {
                String line = outputLines.get(i);
                if (line.equals(String.format("\"%d\"", appId))) {
                    int nextEndBlock = findNextEndBlock(i, outputLines);

                    outputPerAppId.put(appId, outputLines.subList(i, nextEndBlock));
                    break;
                }
            }
        }

        return outputPerAppId;
    }

    private int findNextEndBlock(int startingPosition, List<String> outputLines) {
        for (int i = startingPosition; i < outputLines.size(); i++) {
            String line = outputLines.get(i);
            if (line.equals("}")) {
                return i + 1;
            }
        }
        return -1;
    }
}
