package com.selesse.steam.registry;

public class RegistryNotFoundException extends RuntimeException {
    public RegistryNotFoundException() {}

    public RegistryNotFoundException(String message) {
        super(message);
    }
}
