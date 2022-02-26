package com.selesse.steam.games;

import com.google.common.annotations.VisibleForTesting;
import com.selesse.steam.SteamAccountId;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class RemoteGameListFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteGameListFetcher.class);
    private final SteamAccountId accountId;

    public RemoteGameListFetcher(SteamAccountId accountId) {
        this.accountId = accountId;
    }

    public List<Long> fetchGameIdList() {
        var endpoint = "https://steamcommunity.com/profiles/%s/games?xml=1".formatted(accountId.to64Bit());
        try {
            String output = getOutputFromHttpConnection(endpoint);

            var appIdList = getAppIdList(accountId, output);
            return appIdList.stream().map(Long::parseLong).collect(Collectors.toList());
        } catch (InterruptedException | IOException e) {
            return new ArrayList<>();
        }
    }

    private List<String> getAppIdList(SteamAccountId steamAccountId, String output) {
        List<String> appIds = new ArrayList<>();
        try {
            DocumentBuilder documentBuilder =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document =
                    documentBuilder.parse(new ByteArrayInputStream(output.getBytes(StandardCharsets.UTF_8)));
            XPath xPath = XPathFactory.newInstance().newXPath();
            var errorMessage = xPath.compile("gamesList/error").evaluate(document);

            if (errorMessage.equals("This profile is private")) {
                LOGGER.info("Profile for user {} was private", steamAccountId);
                return appIds;
            }

            var result = xPath.compile("gamesList/games/game/appID").evaluate(document, XPathConstants.NODESET);
            if (result instanceof NodeList nodes) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    appIds.add(node.getChildNodes().item(0).getNodeValue());
                }
            }

            return appIds;
        } catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {
            LOGGER.error("Error trying to parse game list, returning empty list", e);
            return appIds;
        }
    }

    @VisibleForTesting
    String getOutputFromHttpConnection(String endpoint) throws InterruptedException, IOException {
        HttpClient httpClient = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create(endpoint)).build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines());
        return String.join("\n", response.body().toList());
    }
}
