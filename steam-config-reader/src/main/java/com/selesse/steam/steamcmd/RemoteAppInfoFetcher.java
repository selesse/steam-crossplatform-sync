package com.selesse.steam.steamcmd;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

public class RemoteAppInfoFetcher {
    public List<String> fetch(Long appId) {
        try {
            String server = System.getenv().get("APP_INFO_PRINT_HOST");
            HttpClient httpClient = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder(
                    URI.create(server + "/app-info/" + appId)
            ).build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines());
            return response.body().collect(Collectors.toList());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
