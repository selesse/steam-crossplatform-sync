package com.selesse.steam.crossplatform.sync.serialize;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SyncableGameRaw(String windows, String mac, String linux, String name, long gameId) {}
