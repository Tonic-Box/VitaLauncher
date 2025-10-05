package com.tonic.launcher.util;

import com.tonic.launcher.LauncherMain;
import com.tonic.launcher.ui.SplashScreen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UpdateProcessor
{
    public static boolean process(SplashScreen splash) throws Exception
    {
        Update response = Update.checkForUpdates();
        if(response == Update.NO_UPDATE)
            return true;

        if(response == Update.UPDATE_WAITING)
            return false;

        splash.setStatusText("Downloading VitaLite " + Versioning.getLiveVitaLiteVersion() + "...");
        writeVersion();
        downloadLatestVitaLite();
        return true;
    }

    private static void writeVersion() throws IOException {
        Path filePath = Path.of(LauncherMain.VITA_DIR.toString(), "version.txt");
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, Versioning.getLiveVitaLiteVersion(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void downloadLatestVitaLite() throws Exception {
        String tag = Versioning.getLiveVitaLiteVersion();
        String zipFileName = "VitaLite-" + tag + ".zip";
        String downloadUrl = String.format(
                "https://github.com/Tonic-Box/VitaLite/releases/download/%s/%s",
                tag, zipFileName
        );

        Files.createDirectories(LauncherMain.VITA_DIR);
        Path tempZip = Files.createTempFile("vitalite-", ".zip");

        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(downloadUrl))
                    .header("User-Agent", "VitaLite-Updater/1.0")
                    .build();

            HttpResponse<Path> response = client.send(request,
                    HttpResponse.BodyHandlers.ofFile(tempZip));

            if (response.statusCode() != 200) {
                throw new IOException("Download failed: HTTP " + response.statusCode());
            }

            try (ZipFile zipFile = new ZipFile(tempZip.toFile())) {
                ZipEntry jarEntry = zipFile.getEntry("VitaLite.jar");
                if (jarEntry == null) {
                    jarEntry = zipFile.stream()
                            .filter(e -> e.getName().endsWith("VitaLite.jar"))
                            .findFirst()
                            .orElseThrow(() -> new IOException("VitaLite.jar not found in zip"));
                }

                Path jarPath = LauncherMain.VITA_DIR.resolve("VitaLite.jar");
                try (InputStream in = zipFile.getInputStream(jarEntry)) {
                    Files.copy(in, jarPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Download was interrupted", e);
        } finally {
            Files.deleteIfExists(tempZip);
        }
    }
}
