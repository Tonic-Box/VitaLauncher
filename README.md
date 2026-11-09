# VitaLite Launcher
The launcher for [VitaLite](https://github.com/Tonic-Box/VitaLite). It keeps VitaLite up to date with the latest release and launches it with your chosen settings.
- Downloads and updates VitaLite automatically from the latest GitHub release
- Falls back to the previous VitaLite version while an update for a new RuneLite revision is not yet out
- Downloads its own JDK 11 for running the client, no separate setup needed
- Saves launch options, JVM arguments and memory settings between launches

## Download
Download the `VitaLauncher-<version>.jar` from the [most recent release](https://github.com/Tonic-Box/VitaLauncher/releases/latest).

**Requirements:** Java 11 or newer to run the launcher

Run it by double clicking the jar, or from a terminal with `java -jar VitaLauncher-<version>.jar`

## Launch Settings
| Option          | Description                                                    |
|-----------------|----------------------------------------------------------------|
| Disable Plugins | Disables loading of core plugins                               |
| Minimal Memory  | Runs the client with minimal allotted memory                   |
| Disable Music   | Prevents the loading of music tracks                           |
| Incognito Mode  | Visually display as 'RuneLite' instead of 'VitaLite'           |
| Disable RL DLL  | Disables RuneLite's mouse hook DLL from being loaded or called |
| RS Dump Path    | Path to dump the gamepack to                                   |
| Proxy Server    | Proxy server to use (ip:port or ip:port:username:password)     |
| Login           | Legacy (user:pass) or Jagex (sessionID:characterID:displayName, or a path to a runelite credentials file) |
| Max Memory      | Maximum JVM memory in MB (-Xmx)                                |
| JVM Arguments   | Extra arguments passed to the client JVM                       |

## Files
Everything the launcher manages lives in `~/.runelite/vitalite`:
- `VitaLite.jar` the client
- `version.txt` the installed VitaLite release
- `jdk` the JDK used to run the client
- `LauncherConfig` saved launcher settings

## Building from source
**Requirements:** Jdk 11
1. Run the `build` gradle task
2. The runnable jar is written to `build/libs/VitaLauncher-<version>-shaded.jar`

## Disclaimer

VitaLite is a third-party loader for RuneLite. Use at your own risk. The developers are not responsible for any consequences resulting from the use of this software.

## [Buy me a coffee](https://ko-fi.com/tonicbox)
