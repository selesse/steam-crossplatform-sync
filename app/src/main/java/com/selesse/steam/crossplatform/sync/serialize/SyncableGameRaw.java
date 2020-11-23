package com.selesse.steam.crossplatform.sync.serialize;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SyncableGameRaw {
    public String windows;
    public String mac;
    public String name;
    public long gameId;
}
