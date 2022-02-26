package com.selesse.files;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class FileModified {
    public static long getHoursSinceModification(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("File " + file + " does not exist");
        }
        return getUnitSinceModified(ChronoUnit.HOURS, file.lastModified());
    }

    public static long getDaysSinceModification(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("File " + file + " does not exist");
        }
        return getUnitSinceModified(ChronoUnit.DAYS, file.lastModified());
    }

    private static long getUnitSinceModified(ChronoUnit chronoUnit, long lastModified) {
        LocalDateTime localLastModified =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified), ZoneId.systemDefault());
        return Math.abs(chronoUnit.between(LocalDateTime.now(), localLastModified));
    }
}
