package com.selesse.steam.games.saves;

public class UnableToParseSaveException extends RuntimeException {
    public UnableToParseSaveException(String gameName) {
        super("Unable to parse config for " + gameName);
    }
}
