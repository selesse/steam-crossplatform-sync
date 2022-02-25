package com.selesse.steam.steamcmd;

import com.google.common.base.Splitter;
import com.selesse.os.OperatingSystems;
import com.selesse.processes.ProcessRunner;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PrintAppInfoExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrintAppInfoExecutor.class);

    public List<String> runPrintAppInfoProcess(Long appId) {
        ProcessRunner processRunner =
                new ProcessRunner("steamcmd", "+login", "anonymous", "+app_info_print", appId.toString(), "+quit");
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
}
