package com.selesse.steam.steamcmd.remote;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class RemoteAppInfoFetcher {
    private final String remoteAppInfoUrl;

    public RemoteAppInfoFetcher(String remoteAppInfoUrl) {
        this.remoteAppInfoUrl = remoteAppInfoUrl;
    }

    public List<String> fetch(Long appId) {
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder(
                    URI.create(remoteAppInfoUrl + "/app-info/" + appId)
            ).build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines());
            return response.body().toList();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
