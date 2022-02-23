package com.selesse.steam.applist;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SteamNameAndId(String name, @JsonProperty(value="appid") long appId) {}
