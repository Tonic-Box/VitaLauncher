package com.tonic.launcher.util;

import java.io.*;
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
     * @return Path to java executable
     * @throws IOException if download or extraction fails
     */
    public static Path ensureJDK() throws IOException {
        Path javaExecutable = getJavaExecutable();

        if (Files.exists(javaExecutable)) {
            System.out.println("JDK found at " + JDK_DIR);
            return javaExecutable;
        }

        System.out.println("JDK not found. Downloading JDK " + JDK_VERSION + "...");
        downloadAndExtractJDK();

        if (!Files.exists(javaExecutable)) {
            throw new IOException("JDK installation failed - Java executable not found after extraction");
        }

        System.out.println("JDK installed successfully!");
        return javaExecutable;
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

    private static void downloadAndExtractJDK() throws IOException {
        Files.createDirectories(JDK_DIR);

        String downloadUrl = getDownloadURL();
        Path tempFile = JDK_DIR.resolve("jdk-temp" + getArchiveExtension());

        // Download
        downloadFile(downloadUrl, tempFile);

        // Extract
        extractArchive(tempFile);

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

    private static void downloadFile(String url, Path destination) throws IOException {
        System.out.println("Downloading from: " + url);

        try (InputStream in = new URL(url).openStream();
             OutputStream out = Files.newOutputStream(destination)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;

                // Print progress every 10MB
                if (totalBytes % (10 * 1024 * 1024) == 0) {
                    System.out.println("Downloaded " + (totalBytes / (1024 * 1024)) + " MB...");
                }
            }
        }
    }

    private static void extractArchive(Path archivePath) throws IOException {
        System.out.println("Extracting JDK...");

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            extractZip(archivePath);
        } else {
            extractTarGz(archivePath);
        }
    }

    private static void extractZip(Path zipPath) throws IOException {
        Path tempDir = JDK_DIR.resolve("temp");

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path destPath = tempDir.resolve(entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(destPath);
                } else {
                    Files.createDirectories(destPath.getParent());
                    Files.copy(zis, destPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }

        // Move contents from nested folder to JDK_DIR
        moveFromNestedFolder(tempDir);
    }

    private static void extractTarGz(Path tarGzPath) throws IOException {
        Path tempDir = JDK_DIR.resolve("temp");

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

                    // Preserve executable permissions on Unix
                    if (!System.getProperty("os.name").toLowerCase().contains("win")) {
                        if (entry.getMode() != 0) {
                            destPath.toFile().setExecutable((entry.getMode() & 0100) != 0);
                        }
                    }
                }
            }
        }

        // Move contents from nested folder to JDK_DIR
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