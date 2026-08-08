package com.selesse.steam.crossplatform.sync;

/**
 * A command line this program can't act on. The message says what's wrong with it; the usage text
 * printed alongside says what would have been allowed.
 */
class UsageException extends RuntimeException {
    UsageException(String message) {
        super(message);
    }
}
