package com.selesse.files;

import java.time.LocalDateTime;

public class LastModifiedResult {
    private LocalDateTime lastModified;
    private boolean exists;

    private LastModifiedResult(LocalDateTime lastModified, boolean exists) {
        this.lastModified = lastModified;
        this.exists = exists;
    }

    public boolean exists() {
        return exists;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public static LastModifiedResult doesNotExist() {
        return new LastModifiedResult(null, false);
    }

    public static LastModifiedResult of(LocalDateTime ofInstant) {
        return new LastModifiedResult(ofInstant, true);
    }
}
