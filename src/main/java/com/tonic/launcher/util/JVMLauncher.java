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
    /**
     * LauncheXmxs an external JAR file in a new JVM process and exits current JVM
     *
     * @param jvmArgs     JVM arguments to pass to the java process
     * @param programArgs Arguments to pass to the main method
     * @param callback    Callback to run after launching the new process (can be null)
     */
    public static void launchExternalJar(List<String> jvmArgs, List<String> programArgs, Runnable callback) throws IOException {

        File jarFile = new File(LauncherMain.VITA_DIR  + File.separator + "VitaLite.jar");
        if (!jarFile.exists()) {
            throw new IOException("VitaLite JAR file not found");
        }

        String os = System.getProperty("os.name").toLowerCase();
        String javaBin = LauncherMain.JDK_BIN_DIR + File.separator + "java";
        if(os.contains("mac"))
        {
            javaBin = LauncherMain.JDK_BIN_DIR_MAC + File.separator + "java";
        }
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            javaBin += ".exe";
        }

        List<String> command = new ArrayList<>();
        command.add(javaBin);
        if (jvmArgs != null && !jvmArgs.isEmpty()) {
            command.addAll(jvmArgs);
        }
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
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT); // VitaLite can print to console
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);  // VitaLite can print errors
        processBuilder.start();

        Thread listenerThread = new Thread(() -> {
            try (ServerSocket ss = serverSocket; // Close ServerSocket when done
                 Socket socket = ss.accept();
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(socket.getInputStream()))) {

                String message = reader.readLine();
                if (message != null && message.equals("Done")) {
                    callback.run();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "LauncherComThread");
        listenerThread.setDaemon(true); // Don't prevent JVM shutdown
        listenerThread.start();
    }
}