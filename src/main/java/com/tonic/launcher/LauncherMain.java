package com.tonic.launcher;

import com.tonic.launcher.ui.LauncherSettingsPanel;
import com.tonic.launcher.ui.SplashScreen;
import com.tonic.launcher.util.JDKManager;
import com.tonic.launcher.util.JVMLauncher;
import com.tonic.launcher.util.LauncherConfig;
import com.tonic.launcher.util.UpdateProcessor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class LauncherMain {
    public static final Path VITA_DIR = Path.of(System.getProperty("user.home"), ".runelite", "vitalite");
    public static final Path JDK_DIR = Path.of(VITA_DIR.toString(), "jdk");
    public static final Path JDK_DIR_MAC = Path.of(VITA_DIR.toString(), "jdk", "Contents", "Home");
    public static final Path JDK_BIN_DIR = Path.of(JDK_DIR.toString(), "bin");
    public static final Path JDK_BIN_DIR_MAC = Path.of(JDK_DIR_MAC.toString(), "bin");
    private static SplashScreen splash;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LauncherSettingsPanel settingsPanel = new LauncherSettingsPanel();
            setFrameIcon(settingsPanel);

            settingsPanel.setLaunchCallback(cliArgs -> {
                System.out.println("Launching with arguments: " + String.join(" ", cliArgs));
                startLaunchSequence(cliArgs);
            });

            settingsPanel.setVisible(true);
        });
    }

    private static void startLaunchSequence(List<String> cliArgs) {
        SwingUtilities.invokeLater(() -> {
            splash = new SplashScreen();
            splash.setVisible(true);
            new Thread(() -> {
                try {
                    performLoadingSequence(cliArgs);
                } catch (Exception e) {
                    e.printStackTrace();
                    splash.setError("An error occurred: " + e.getMessage());
                }
            }).start();
        });
    }

    private static void performLoadingSequence(List<String> cliArgs) throws Exception {
        // JDK: 0-40% overall
        JDKManager.ensureJDK(splash);

        // Updates: 40-70% overall
        splash.setProgress(0, 40, "Checking for updates...");
        if(!UpdateProcessor.process(splash))
        {
            splash.setError("An update is required but not yet available. Please try again later.");
            return;
        }

        // Launching: 70-99% overall (stay at 99% until callback confirms launch)
        splash.setProgress(100, 99, "Launching VitaLite...");

        // Load JVM args from config
        LauncherConfig config = new LauncherConfig();
        List<String> jvmArgs = config.getJvmArgs();

        JVMLauncher.launchExternalJar(jvmArgs, cliArgs, () -> {
            // Only reaches 100% when VitaLite confirms it's ready
            splash.setProgress(100, 100, "Launch complete!");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            System.exit(0);
        });
    }

    private static void setFrameIcon(JFrame frame) {
        try {
            BufferedImage icon = ImageIO.read(
                    LauncherMain.class.getResourceAsStream("/com/tonic/launcher/window_icon.png")
            );
            if (icon != null) {
                frame.setIconImage(icon);
            }
        } catch (IOException e) {
            System.err.println("Failed to load window icon: " + e.getMessage());
        }
    }
}
