package com.selesse.steam.steamcmd;

import com.google.common.base.Splitter;
import com.selesse.processes.ProcessRunner;

import java.util.List;

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
}
