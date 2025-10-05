package com.tonic.launcher.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tonic.launcher.LauncherMain;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;


public final class Versioning
{
    private Versioning() {
        // Utility class - prevent instantiation
    }

    public static String getVitaLiteVersion()
    {
        File file = new File(LauncherMain.VITA_DIR.toString(), "version.txt");
        if (!file.exists() || !file.isFile()) {
            return "0.0.0";
        }

        try {
            return Files.readString(file.toPath()).trim();
        } catch (IOException e) {
            return "0.0.0";
        }
    }

    public static String getLiveRuneliteVersion()
    {
        return RuneliteConfigUtil.getTagValueFromURL("release");
    }

    /**
     * Fetches the latest VitaLite release tag from GitHub API.
     * @return the latest release tag as a string
     * @throws IOException if the API request fails
     */
    public static String getLiveVitaLiteVersion() throws IOException {
        final String apiUrl = "https://api.github.com/repos/Tonic-Box/VitaLite/releases/latest";
        final HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        try {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("User-Agent", "VitaLite-Versioning/1.0")
                    .header("Accept", "application/vnd.github.v3+json")
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            final HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("GitHub API request failed: HTTP " + response.statusCode());
            }

            final JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            return json.get("tag_name").getAsString();

        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request was interrupted", e);
        }
    }
}