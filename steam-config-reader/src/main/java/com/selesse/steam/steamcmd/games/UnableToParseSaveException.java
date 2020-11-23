package com.selesse.steam.steamcmd.games;

public class UnableToParseSaveException extends RuntimeException {
    public UnableToParseSaveException(String gameName) {
        super("Unable to parse config for " + gameName);
    }
}
