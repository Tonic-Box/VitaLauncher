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
        {
            if(splash != null) {
                splash.setProgress(100, 70, "VitaLite is up to date");
            }
            return true;
        }

        if(response == Update.UPDATE_WAITING)
        {
            splash.setProgress(0, 40, "An update is required but not yet available. Falling back to previous version.");
            String liveVita = Versioning.getLiveVitaLiteVersion();
            String current = Versioning.getVitaLiteVersion();
            if(!liveVita.equals(current)) {
                downloadLatestVitaLite(splash);
                writeVersion();
            }
            return false;
        }

        if(splash != null) {
            splash.setProgress(0, 40, "Downloading VitaLite " + Versioning.getLiveVitaLiteVersion() + "...");
        }
        downloadLatestVitaLite(splash);
        writeVersion();

        if(splash != null) {
            splash.setProgress(100, 70, "VitaLite updated successfully!");
        }
        return true;
    }

    private static void writeVersion() throws IOException {
        Path filePath = Path.of(LauncherMain.VITA_DIR.toString(), "version.txt");
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, Versioning.getLiveVitaLiteVersion(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void downloadLatestVitaLite(SplashScreen splash) throws Exception {
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

            // Download with progress tracking
            HttpResponse<InputStream> response = client.send(request,
                    HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                throw new IOException("Download failed: HTTP " + response.statusCode());
            }

            long fileSize = response.headers().firstValueAsLong("Content-Length").orElse(-1);

            try (InputStream in = response.body();
                 var out = Files.newOutputStream(tempZip)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;
                long lastUpdate = 0;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;

                    // Update UI every 256KB
                    if (splash != null && (totalBytes - lastUpdate) > (256 * 1024)) {
                        // Stage progress: actual download percentage (0-100%)
                        int stagePercent = fileSize > 0 ? (int) ((totalBytes * 100) / fileSize) : 0;

                        // Overall progress: VitaLite download is 40-65% of total launcher process
                        int overallPercent = 40 + ((stagePercent * 25) / 100);

                        String statusText = String.format("Downloading VitaLite: %d/%d MB (%d%%)",
                                totalBytes / (1024 * 1024),
                                fileSize > 0 ? fileSize / (1024 * 1024) : 0,
                                stagePercent);

                        splash.setProgress(stagePercent, overallPercent, statusText);
                        lastUpdate = totalBytes;
                    }
                }
            }

            if (splash != null) {
                splash.setProgress(100, 65, "Extracting VitaLite...");
            }

            // Extract VitaLite.jar from zip
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