package com.tonic.launcher.util;

import java.util.Arrays;
import java.util.List;

public class LauncherConfig
{
    private final ConfigManager config = new ConfigManager("LauncherConfig");

    // Default JVM arguments
    private static final List<String> DEFAULT_JVM_ARGS = Arrays.asList(
            "-XX:+DisableAttachMechanism",
            "-Drunelite.launcher.blacklistedDlls=RTSSHooks.dll,RTSSHooks64.dll,NahimicOSD.dll,NahimicMSIOSD.dll,Nahimic2OSD.dll,Nahimic2DevProps.dll,k_fps32.dll,k_fps64.dll,SS2DevProps.dll,SS2OSD.dll,GTIII-OSD64-GL.dll,GTIII-OSD64-VK.dll,GTIII-OSD64.dll",
            "-XX:CompileThreshold=1500",
            "-XX:+UseSerialGC",
            "-XX:+UseStringDeduplication",
            "-Xms256m",
            "-Xmn96m",
            "-XX:MinHeapFreeRatio=10",
            "-XX:MaxHeapFreeRatio=25",
            "-XX:TieredStopAtLevel=1"
    );

    private static final List<String> PREVIOUS_DEFAULT_JVM_ARGS = DEFAULT_JVM_ARGS.subList(0, 5);

    public boolean isNoMusic()
    {
        return config.getBooleanOrDefault("noMusic", false);
    }

    public void setNoMusic(boolean noMusic)
    {
        config.setProperty("noMusic", noMusic);
    }

    public boolean isNoSound()
    {
        return config.getBooleanOrDefault("noSound", false);
    }

    public void setNoSound(boolean noSound)
    {
        config.setProperty("noSound", noSound);
    }

    public boolean isMin()
    {
        return config.getBooleanOrDefault("min", false);
    }

    public void setMin(boolean min)
    {
        config.setProperty("min", min);
    }

    public boolean isNoPlugins()
    {
        return config.getBooleanOrDefault("noPlugins", false);
    }

    public void setNoPlugins(boolean noPlugins)
    {
        config.setProperty("noPlugins", noPlugins);
    }

    public boolean isRsDump()
    {
        return config.getBooleanOrDefault("rsDump", false);
    }

    public void setRsDump(boolean rsDump)
    {
        config.setProperty("rsDump", rsDump);
    }

    public String getRsDumpPath()
    {
        return config.getStringOrDefault("rsDumpPath", "");
    }

    public void setRsDumpPath(String path)
    {
        config.setProperty("rsDumpPath", path);
    }

    public boolean isProxy()
    {
        return config.getBooleanOrDefault("proxy", false);
    }

    public void setProxy(boolean proxy)
    {
        config.setProperty("proxy", proxy);
    }

    public String getProxyData()
    {
        return config.getStringOrDefault("proxyData", "");
    }

    public void setProxyData(String proxyData)
    {
        config.setProperty("proxyData", proxyData);
    }

    public boolean isMouseHook()
    {
        return config.getBooleanOrDefault("mouseHook", false);
    }

    public void setMouseHook(boolean hook)
    {
        config.setProperty("mouseHook", hook);
    }

    public String getLoginString()
    {
        return config.getStringOrDefault("loginString", "");
    }

    public void setLoginString(String loginString)
    {
        config.setProperty("loginString", loginString);
    }

    public String getLoginType()
    {
        return config.getStringOrDefault("loginType", "Legacy");
    }

    public void setLoginType(String loginType)
    {
        config.setProperty("loginType", loginType);
    }

    public boolean isMaxMemoryEnabled()
    {
        return config.getBooleanOrDefault("maxMemoryEnabled", false);
    }

    public void setMaxMemoryEnabled(boolean enabled)
    {
        config.setProperty("maxMemoryEnabled", enabled);
    }

    public int getMaxMemoryValue()
    {
        return config.getIntOrDefault("maxMemoryValue", 768);
    }

    public void setMaxMemoryValue(int value)
    {
        config.setProperty("maxMemoryValue", value);
    }

    /**
     * Get JVM arguments from config, returns defaults if none are saved or the saved ones are the previous
     * defaults, so lists nobody customised pick up new defaults
     * @return list of JVM arguments
     */
    public List<String> getJvmArgs()
    {
        List<String> args = config.getStringList("jvmArgs");
        if (args.isEmpty() || args.equals(PREVIOUS_DEFAULT_JVM_ARGS)) {
            return DEFAULT_JVM_ARGS;
        }
        return args;
    }

    /**
     * Set JVM arguments in config
     * @param args list of JVM arguments
     */
    public void setJvmArgs(List<String> args)
    {
        config.setList("jvmArgs", args);
    }

    /**
     * Get default JVM arguments
     * @return list of default JVM arguments
     */
    public static List<String> getDefaultJvmArgs()
    {
        return DEFAULT_JVM_ARGS;
    }
}
