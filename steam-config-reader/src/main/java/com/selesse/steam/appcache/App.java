package com.selesse.steam.appcache;

public record App(
        int appId,
        int size,
        int infoState,
        int lastUpdated,
        long picsToken,
        byte[] sha1,
        int changeNumber,
        byte[] sha1Binary,
        VdfObject vdfObject) {}
