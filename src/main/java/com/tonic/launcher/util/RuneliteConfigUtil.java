package com.tonic.launcher.util;

import com.google.gson.JsonParser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RuneliteConfigUtil
{
    public static String getRuneLiteVersion() {
        try {
            URL url = new URL("https://static.runelite.net/bootstrap.json");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
                return JsonParser.parseString(json.toString())
                        .getAsJsonObject()
                        .get("version")
                        .getAsString();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "unknown";
    }
}