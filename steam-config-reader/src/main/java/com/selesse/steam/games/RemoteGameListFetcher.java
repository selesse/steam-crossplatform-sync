package com.selesse.steam.games;

import com.google.common.annotations.VisibleForTesting;
import com.selesse.steam.SteamAccountId;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteGameListFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteGameListFetcher.class);
    private final SteamAccountId accountId;
    private String output;

    public RemoteGameListFetcher(SteamAccountId accountId) {
        this.accountId = accountId;
    }

    public List<Long> fetchGameIdList() {
        var endpoint = "https://steamcommunity.com/profiles/%s/games?xml=1".formatted(accountId.to64Bit());
        try {
            output = getOutputFromHttpConnection(endpoint);

            return new XmlGamesParser().getAppIdList(accountId, output);
        } catch (InterruptedException | IOException e) {
            return new ArrayList<>();
        }
    }

    public String getOutput() {
        return output;
    }

    @VisibleForTesting
    String getOutputFromHttpConnection(String endpoint) throws InterruptedException, IOException {
        HttpClient httpClient = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create(endpoint)).build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines());
        return String.join("\n", response.body().toList());
    }
}
