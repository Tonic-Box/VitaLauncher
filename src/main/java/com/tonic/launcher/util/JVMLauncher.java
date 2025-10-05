package com.tonic.launcher.util;

import com.tonic.launcher.LauncherMain;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JVMLauncher {
    private static final String[] jvmArgs = {
            "-XX:+DisableAttachMechanism",
            "-Drunelite.launcher.blacklistedDlls=RTSSHooks.dll,RTSSHooks64.dll,NahimicOSD.dll,NahimicMSIOSD.dll,Nahimic2OSD.dll,Nahimic2DevProps.dll,k_fps32.dll,k_fps64.dll,SS2DevProps.dll,SS2OSD.dll,GTIII-OSD64-GL.dll,GTIII-OSD64-VK.dll,GTIII-OSD64.dll",
            "-Xmx768m",
            "-Xss2m",
            "-XX:CompileThreshold=1500",
            "-XX:+UseSerialGC",
            "-XX:+UseStringDeduplication"
    };
    /**
     * Launches an external JAR file in a new JVM process and exits current JVM
     *
     * @param programArgs Arguments to pass to the main method
     * @param callback    Callback to run after launching the new process (can be null)
     */
    public static void launchExternalJar(List<String>  programArgs, Runnable callback) throws IOException {

        File jarFile = new File(LauncherMain.VITA_DIR  + File.separator + "VitaLite.jar");
        if (!jarFile.exists()) {
            throw new IOException("VitaLite JAR file not found");
        }

        String javaBin = LauncherMain.JDK_BIN_DIR + File.separator + "java";
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            javaBin += ".exe";
        }

        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.addAll(Arrays.asList(jvmArgs));
        command.add("-jar");
        command.add(jarFile.getAbsolutePath());
        command.add("-safeLaunch");
        if (programArgs != null && !programArgs.isEmpty()) {
            command.addAll(programArgs);
        }

        ServerSocket serverSocket = new ServerSocket(0); // Random port
        int port = serverSocket.getLocalPort();
        command.add("--launcherCom");
        command.add(String.valueOf(port));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.inheritIO();
        processBuilder.start();

        new Thread(() -> {
            try (Socket socket = serverSocket.accept();
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(socket.getInputStream()))) {

                String message = reader.readLine();
                if (message != null && message.equals("Done")) {
                    callback.run();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}