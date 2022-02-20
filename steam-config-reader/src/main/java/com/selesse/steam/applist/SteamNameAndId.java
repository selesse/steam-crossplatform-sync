package com.selesse.steam.applist;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SteamNameAndId {
    public String name;
    @JsonProperty("appid")
    public long appId;
}
