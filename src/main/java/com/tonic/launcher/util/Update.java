package com.tonic.launcher.util;

public enum Update {
    /**
     * Everything is up-to-date.
     */
    NO_UPDATE,

    /**
     * A new version is available for immediate update.
     */
    UPDATE_AVAILABLE,

    /**
     * An update is required but not yet available.
     */
    UPDATE_WAITING
    ;

    /**
     * Checks for updates by comparing the current version with the live versions.
     *
     * The logic is as follows:
     * - NO_UPDATE: Everything is up-to date.
     * - UPDATE_AVAILABLE: A new version is available for immediate update.
     * - UPDATE_WAITING: An update is required but not yet available.
     *
     * @return Update status indicating if an update is available, waiting, or not needed.
     * @throws Exception if there is an error fetching the live versions.
     */
    public static Update checkForUpdates() throws Exception
    {
        String liveRunelite = Versioning.getLiveRuneliteVersion();
        String liveVita = Versioning.getLiveVitaLiteVersion();
        String current = Versioning.getVitaLiteVersion();

        if(!current.startsWith(liveRunelite))
        {
            if(liveVita.startsWith(liveRunelite))
            {
                return UPDATE_AVAILABLE;
            }
            else
            {
                return UPDATE_WAITING;
            }
        }

        if(!current.equals(liveVita))
        {
            return UPDATE_AVAILABLE;
        }

        return NO_UPDATE;
    }
}
