package com.selesse.steam.steamcmd;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.selesse.processes.ProcessRunner;

import java.util.List;
import java.util.Map;

class PrintAppInfoExecutor {
    public List<String> runPrintAppInfoProcess(Long appId) {
        ProcessRunner processRunner = new ProcessRunner(
                "steamcmd", "+login", "anonymous", "+app_info_print", appId.toString(), "+quit"
        );
        String output = processRunner.runAndGetOutput();
        List<String> lines = Splitter.on("\n").splitToList(output);
        int firstLine = lines.indexOf(String.format("\"%d\"", appId));
        return lines.subList(firstLine, lines.size());
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
