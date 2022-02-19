package com.selesse.steam;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import com.selesse.steam.applist.SteamAppList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
            URL steamApiList = new URL("https://api.steampowered.com/ISteamApps/GetAppList/v2/");
            HttpURLConnection urlConnection = (HttpURLConnection) steamApiList.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            String appListString = CharStreams.toString(new InputStreamReader(inputStream));
            ObjectMapper objectMapper = new ObjectMapper();
            JsonParser jsonParser = objectMapper.readTree(appListString).path("applist").traverse();
            return objectMapper.readValue(jsonParser, SteamAppList.class);
        } catch (IOException e) {
            LOGGER.info("Unable to fetch app list", e);
        }
        return new SteamAppList();
    }
}
