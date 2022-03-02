package com.selesse.steam.crossplatform.sync.serialize;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "gameId", "windows", "mac", "linux", "sync"})
@JsonIgnoreProperties(ignoreUnknown = true)
public record SyncableGameRaw(
        List<String> windows, List<String> mac, List<String> linux, String name, long gameId, boolean sync) {}
