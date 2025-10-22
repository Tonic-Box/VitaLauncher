package com.tonic.launcher.util;

import com.tonic.launcher.ui.SplashScreen;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import static com.tonic.launcher.LauncherMain.*;

public class JDKManager {

    private static final String JDK_VERSION = "11.0.19+7";
    private static final String JDK_VERSION_URL = "jdk-11.0.19%2B7"; // URL encoded

    /**
     * Ensures JDK is installed, downloading if necessary
     * @param splash The splash screen to update with progress (can be null)
     * @return Path to java executable
     * @throws IOException if download or extraction fails
     */
    public static Path ensureJDK(SplashScreen splash) throws IOException {
        Path javaExecutable = getJavaExecutable();

        if (Files.exists(javaExecutable)) {
            System.out.println("JDK found at " + JDK_DIR);
            if (splash != null) {
                splash.setProgress(100, 40, "JDK 11 ready");
            }
            return javaExecutable;
        }

        System.out.println("JDK not found. Downloading JDK " + JDK_VERSION + "...");
        downloadAndExtractJDK(splash);

        if (!Files.exists(javaExecutable)) {
            throw new IOException("JDK installation failed - Java executable not found after extraction");
        }

        System.out.println("JDK installed successfully!");
        return javaExecutable;
    }

    /**
     * Overload for backward compatibility
     */
    public static Path ensureJDK() throws IOException {
        return ensureJDK(null);
    }

    private static Path getJavaExecutable() {
        String os = System.getProperty("os.name").toLowerCase();
        String javaExe = os.contains("win") ? "java.exe" : "java";
        if(os.contains("mac")) {
            // On macOS, the java executable is located in Contents/Home/bin
            return JDK_BIN_DIR_MAC.resolve(javaExe);
        }
        return JDK_BIN_DIR.resolve(javaExe);
    }

    private static void downloadAndExtractJDK(SplashScreen splash) throws IOException {
        Files.createDirectories(JDK_DIR);

        String downloadUrl = getDownloadURL();
        Path tempFile = JDK_DIR.resolve("jdk-temp" + getArchiveExtension());

        // Download with progress updates
        downloadFile(downloadUrl, tempFile, splash);

        // Extract
        extractArchive(tempFile, splash);

        // Clean up
        Files.deleteIfExists(tempFile);
    }

    private static String getDownloadURL() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        String osType;
        if (os.contains("win")) {
            osType = "windows";
        } else if (os.contains("mac")) {
            osType = "mac";
        } else {
            osType = "linux";
        }

        String archType = arch.contains("64") ? "x64" : "x32";

        return String.format(
                "https://api.adoptium.net/v3/binary/version/%s/%s/%s/jdk/hotspot/normal/eclipse",
                JDK_VERSION_URL, osType, archType
        );
    }

    private static String getArchiveExtension() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win") ? ".zip" : ".tar.gz";
    }

    private static void downloadFile(String url, Path destination, SplashScreen splash) throws IOException {
        System.out.println("Downloading from: " + url);

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(30000); // 30 seconds
            connection.setReadTimeout(30000);    // 30 seconds
            connection.setRequestProperty("User-Agent", "VitaLite-JDK-Downloader/1.0");

            connection.connect();

            if (connection.getResponseCode() != 200) {
                throw new IOException("Download failed: HTTP " + connection.getResponseCode());
            }

            long fileSize = connection.getContentLengthLong();
            System.out.println("File size: " + (fileSize / (1024 * 1024)) + " MB");

            try (InputStream in = connection.getInputStream();
                 OutputStream out = Files.newOutputStream(destination)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;
                long lastUpdate = 0;
                long startTime = System.currentTimeMillis();
                long lastSpeedUpdate = startTime;
                long bytesAtLastSpeedUpdate = 0;
                String lastSpeedEta = ""; // Cache speed/ETA to prevent flickering

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;

                    long currentTime = System.currentTimeMillis();

                    // Update speed/ETA every 2 seconds to prevent flickering
                    if (currentTime - lastSpeedUpdate >= 2000) {
                        long bytesSinceLastUpdate = totalBytes - bytesAtLastSpeedUpdate;
                        long timeDiff = currentTime - lastSpeedUpdate;
                        double speedMBps = (bytesSinceLastUpdate / 1024.0 / 1024.0) / (timeDiff / 1000.0);

                        // Calculate ETA
                        String etaStr = "";
                        if (speedMBps > 0.1 && fileSize > 0) {
                            long bytesRemaining = fileSize - totalBytes;
                            double secondsRemaining = (bytesRemaining / 1024.0 / 1024.0) / speedMBps;
                            if (secondsRemaining < 60) {
                                etaStr = String.format("%ds", (int) secondsRemaining);
                            } else {
                                etaStr = String.format("%dm", (int) (secondsRemaining / 60));
                            }
                        }

                        lastSpeedEta = String.format(" • %.1f MB/s • %s left", speedMBps, etaStr);
                        lastSpeedUpdate = currentTime;
                        bytesAtLastSpeedUpdate = totalBytes;
                    }

                    // Update UI every 512KB
                    if (splash != null && (totalBytes - lastUpdate) > (512 * 1024)) {
                        // Stage progress: actual download percentage (0-100%)
                        int stagePercent = fileSize > 0 ? (int) ((totalBytes * 100) / fileSize) : 0;

                        // Overall progress: JDK download is 0-25% of total launcher process
                        int overallPercent = (stagePercent * 25) / 100;

                        String statusText = String.format("Downloading JDK 11: %d/%d MB (%d%%)%s",
                                totalBytes / (1024 * 1024),
                                fileSize > 0 ? fileSize / (1024 * 1024) : 0,
                                stagePercent,
                                lastSpeedEta);

                        splash.setProgress(stagePercent, overallPercent, statusText);
                        lastUpdate = totalBytes;
                    }

                    // Console progress every 10MB
                    if (totalBytes % (10 * 1024 * 1024) < 8192) {
                        System.out.println("Downloaded " + (totalBytes / (1024 * 1024)) + " MB...");
                    }
                }

                System.out.println("Download complete: " + (totalBytes / (1024 * 1024)) + " MB total");

                if (splash != null) {
                    splash.setProgress(100, 25, "Download complete!");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void extractArchive(Path archivePath, SplashScreen splash) throws IOException {
        System.out.println("Extracting JDK...");

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            extractZip(archivePath, splash);
        } else {
            extractTarGz(archivePath, splash);
        }
    }

    private static void extractZip(Path zipPath, SplashScreen splash) throws IOException {
        Path tempDir = JDK_DIR.resolve("temp");

        // First pass: count total files
        int totalFiles = 0;
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            while (zis.getNextEntry() != null) {
                totalFiles++;
                zis.closeEntry();
            }
        }

        System.out.println("Extracting " + totalFiles + " files...");

        // Second pass: extract with progress
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            int fileCount = 0;
            int lastPercent = -1;
            long lastUpdate = System.currentTimeMillis();

            while ((entry = zis.getNextEntry()) != null) {
                Path destPath = tempDir.resolve(entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(destPath);
                } else {
                    Files.createDirectories(destPath.getParent());
                    Files.copy(zis, destPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();

                fileCount++;

                // Update UI every 500ms to avoid spam
                long currentTime = System.currentTimeMillis();
                if (splash != null && totalFiles > 0 && (currentTime - lastUpdate > 500)) {
                    int stagePercent = (fileCount * 100) / totalFiles;

                    // Overall progress: Extraction is 25-40% of total
                    int overallPercent = 25 + ((stagePercent * 15) / 100);

                    if (stagePercent != lastPercent) {
                        splash.setProgress(stagePercent, overallPercent,
                                String.format("Extracting JDK 11: %d/%d files (%d%%)",
                                        fileCount, totalFiles, stagePercent));
                        lastPercent = stagePercent;
                        lastUpdate = currentTime;
                    }
                }
            }
        }

        if (splash != null) {
            splash.setProgress(100, 40, "Finalizing installation...");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        moveFromNestedFolder(tempDir);
    }

    private static void extractTarGz(Path tarGzPath, SplashScreen splash) throws IOException {
        Path tempDir = JDK_DIR.resolve("temp");

        int fileCount = 0;
        int estimatedTotal = 3000;
        int lastPercent = -1;
        long lastUpdate = System.currentTimeMillis();

        try (InputStream fis = Files.newInputStream(tarGzPath);
             GZIPInputStream gzis = new GZIPInputStream(fis);
             TarArchiveInputStream tais = new TarArchiveInputStream(gzis)) {

            TarArchiveEntry entry;

            while ((entry = tais.getNextTarEntry()) != null) {
                Path destPath = tempDir.resolve(entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(destPath);
                } else {
                    Files.createDirectories(destPath.getParent());
                    Files.copy(tais, destPath, StandardCopyOption.REPLACE_EXISTING);

                    if (!System.getProperty("os.name").toLowerCase().contains("win")) {
                        if (entry.getMode() != 0) {
                            destPath.toFile().setExecutable((entry.getMode() & 0100) != 0);
                        }
                    }
                }

                fileCount++;

                // Update UI every 500ms
                long currentTime = System.currentTimeMillis();
                if (splash != null && (currentTime - lastUpdate > 500)) {
                    int stagePercent = Math.min(99, (fileCount * 100) / estimatedTotal);

                    // Overall progress: Extraction is 25-40% of total
                    int overallPercent = 25 + ((stagePercent * 15) / 100);

                    if (stagePercent != lastPercent) {
                        splash.setProgress(stagePercent, overallPercent,
                                String.format("Extracting JDK 11: %d files (%d%%)",
                                        fileCount, stagePercent));
                        lastPercent = stagePercent;
                        lastUpdate = currentTime;
                    }
                }
            }
        }

        if (splash != null) {
            splash.setProgress(100, 40, "Finalizing installation...");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        moveFromNestedFolder(tempDir);
    }

    private static void moveFromNestedFolder(Path tempDir) throws IOException {
        // Find the actual JDK folder (usually jdk-11.0.19+7 or similar)
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempDir)) {
            for (Path nestedDir : stream) {
                if (Files.isDirectory(nestedDir)) {
                    // Move all contents to JDK_DIR
                    try (DirectoryStream<Path> nestedStream = Files.newDirectoryStream(nestedDir)) {
                        for (Path item : nestedStream) {
                            Path target = JDK_DIR.resolve(nestedDir.relativize(item));
                            moveRecursive(item, target);
                        }
                    }
                }
            }
        }

        // Clean up temp directory
        deleteRecursive(tempDir);
    }

    private static void moveRecursive(Path source, Path target) throws IOException {
        if (Files.isDirectory(source)) {
            Files.createDirectories(target);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(source)) {
                for (Path child : stream) {
                    moveRecursive(child, target.resolve(source.relativize(child)));
                }
            }
        } else {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void deleteRecursive(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path child : stream) {
                    deleteRecursive(child);
                }
            }
        }
        Files.deleteIfExists(path);
    }
}