package com.selesse.steam.games;

import com.selesse.steam.SteamAccountId;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

public class XmlGamesParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlGamesParser.class);

    public List<Long> getAppIdList(SteamAccountId steamAccountId, String xmlGames) {
        List<Long> appIds = new ArrayList<>();
        try {
            DocumentBuilder documentBuilder =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document =
                    documentBuilder.parse(new ByteArrayInputStream(xmlGames.getBytes(StandardCharsets.UTF_8)));
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
                    long nodeValue = Long.parseLong(node.getChildNodes().item(0).getNodeValue());
                    appIds.add(nodeValue);
                }
            }

            return appIds;
        } catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {
            LOGGER.error("Error trying to parse game list, returning empty list", e);
            return appIds;
        }
    }

}
