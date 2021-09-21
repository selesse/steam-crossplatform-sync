package com.selesse.steam;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SteamApp {
    public String name;
    @JsonProperty("appid")
    public long appId;
}
