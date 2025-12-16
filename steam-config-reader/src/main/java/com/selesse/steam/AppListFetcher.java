package com.selesse.steam;

import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.selesse.steam.applist.SteamAppList;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class AppListFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppListFetcher.class);

    private static SteamAppList appList;

    public static SteamAppList fetchAppList() {
        if (appList == null) {
            appList = actuallyFetchAppList();
        }
        return appList;
    }

    private static SteamAppList actuallyFetchAppList() {
        try {
            URL steamApiList = URI.create("https://api.steampowered.com/ISteamApps/GetAppList/v2/")
                    .toURL();
            HttpURLConnection urlConnection = (HttpURLConnection) steamApiList.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            String appListString = CharStreams.toString(new InputStreamReader(inputStream));
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode appListNode = objectMapper.readTree(appListString).path("applist");
            return objectMapper.treeToValue(appListNode, SteamAppList.class);
        } catch (IOException e) {
            LOGGER.info("Unable to fetch app list", e);
        }
        return new SteamAppList(Lists.newArrayList());
    }
}
