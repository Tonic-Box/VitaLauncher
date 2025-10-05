package com.tonic.launcher.util;

public class LauncherConfig
{
    private ConfigManager config = new ConfigManager("LauncherConfig");

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

    public boolean isIncognito()
    {
        return config.getBooleanOrDefault("incognito", false);
    }

    public void setIncognito(boolean incognito)
    {
        config.setProperty("incognito", incognito);
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
}
