package com.selesse.steam.appcache;

public record App(int appId, int size, int infoState, int lastUpdated, int picsToken, byte[] sha1, int changeNumber,
                  VdfObject vdfObject) {}
