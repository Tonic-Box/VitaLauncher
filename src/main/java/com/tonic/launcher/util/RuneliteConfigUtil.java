package com.tonic.launcher.util;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RuneliteConfigUtil
{
    /**
     * Extracts the value of the specified tag from an XML file at a given URL.
     *
     * @param tagName   The name of the tag to extract.
     * @return The value of the specified tag, or null if not found.
     */
    public static String getTagValueFromURL(String tagName) {
        try {
            URL url = new URL("https://repo.runelite.net/net/runelite/injected-client/maven-metadata.xml");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == 200) {
                try (InputStream inputStream = connection.getInputStream()) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(inputStream);
                    document.getDocumentElement().normalize();
                    NodeList nodeList = document.getElementsByTagName(tagName);
                    if (nodeList.getLength() > 0) {
                        return nodeList.item(0).getTextContent();
                    }
                }
            } else {
                System.out.println("Failed to fetch XML. HTTP Response Code: " + connection.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}